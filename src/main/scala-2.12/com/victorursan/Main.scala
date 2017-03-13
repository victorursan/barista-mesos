package com.victorursan

import java.net.URI
import java.util
import java.util.{Optional, UUID}

import com.mesosphere.mesos.rx.java.SinkOperations.sink
import com.mesosphere.mesos.rx.java.protobuf.SchedulerCalls.decline
import com.mesosphere.mesos.rx.java.protobuf.{ProtoUtils, ProtobufMesosClientBuilder, SchedulerCalls}
import com.mesosphere.mesos.rx.java.util.UserAgentEntries
import com.mesosphere.mesos.rx.java.{SinkOperation, SinkOperations}
import org.apache.mesos.v1.Protos
import org.apache.mesos.v1.Protos._
import org.apache.mesos.v1.scheduler.Protos.{Call, Event}
import org.slf4j.LoggerFactory
import rx.lang.scala.Observable
//import rx.lang.scala.Observable.just
//import rx.Observable
import rx.Observable.{from, just}
import rx.lang.scala.JavaConversions._

import scala.collection.JavaConverters._

/**
  * Created by victor on 3/6/17.
  */
object Main {
  private val LOGGER = LoggerFactory.getLogger("main")

  def main(args: Array[String]): Unit = {
    val mesosUri = URI.create("http://localhost:8000/mesos/api/v1/scheduler")
    val role = "*"
    val fwName = "Barista"
    val fwId = s"$fwName-${UUID.randomUUID}"
    val user = "root"


    val containerName = "hello-world"
    val containerImage = "victorursan/akka-http-hello"
    val cpusPerTask = 0.3
    val memb = 128.0

    val frameworkID = FrameworkID.newBuilder
      .setValue(fwId)
      .build()

    val stateObject = State[Protos.FrameworkID, Protos.TaskID, Protos.TaskState](frameworkID, containerName, containerImage, role, cpusPerTask, memb)

    val clientBuilder = ProtobufMesosClientBuilder
      .schedulerUsingProtos.mesosUri(mesosUri)
      .applicationUserAgentEntry(UserAgentEntries.literal("com.victorursan", "barista"))

    val subscribeCall = SchedulerCalls.subscribe(
      stateObject.fwId,
      Protos.FrameworkInfo.newBuilder()
        .setId(stateObject.fwId)
        .setUser(user)
        .setName(fwName)
        .setFailoverTimeout(9)
        .setRole(stateObject.resourceRole)
        .build())

    val stateObservable: Observable[State[Protos.FrameworkID, Protos.TaskID, Protos.TaskState]] = just(stateObject).repeat

    clientBuilder
      .subscribe(subscribeCall)
      .processStream { case (unicastEvents: rx.Observable[Event]) =>
        val events: Observable[Event] = toScalaObservable(unicastEvents.share())

        val offerEvaluations: Observable[Optional[SinkOperation[Call]]] =
          events.filter { (event: Event) => event.getType == Event.Type.OFFERS }
            .flatMap { (event: Event) => from(event.getOffers.getOffersList) }
            .zip(stateObservable)
            .map(t => Optional.of(Main.handleOffer(t)))

        val updateStatusAck: Observable[Optional[SinkOperation[Call]]] =
          events.filter((event: Event) =>
            event.getType == Event.Type.UPDATE && event.getUpdate.getStatus.hasUuid)
            .zip(stateObservable)
            .doOnCompleted { (event: Event, state: State[FrameworkID, TaskID, TaskState]) =>
              val status = event.getUpdate.getStatus
              state.put(status.getTaskId, status.getState)
            }
            .map { case (event: Event, state: State[FrameworkID, TaskID, TaskState]) =>
              val status = event.getUpdate.getStatus
              val ack = SchedulerCalls.ackUpdate(state.fwId, status.getUuid, status.getAgentId, status.getTaskId)
              Optional.of(SinkOperations.create(ack))
            }

        val errorLogger: Observable[Optional[SinkOperation[Call]]] =
          events.filter { event => event.getType == Event.Type.ERROR || (event.getType == Event.Type.UPDATE && event.getUpdate.getStatus.getState == TaskState.TASK_ERROR) }
            .doOnNext { _ => LOGGER.warn("Task Error: {}", ProtoUtils.protoToString _) }
            .map(_ => Optional.empty())

        toJavaObservable(
          offerEvaluations
            .merge(updateStatusAck)
            .merge(errorLogger)).asInstanceOf[rx.Observable[Optional[SinkOperation[Call]]]]
      }

    clientBuilder.build.openStream.await()
  }


  private def handleOffer: ((Protos.Offer, State[Protos.FrameworkID, Protos.TaskID, Protos.TaskState])) => SinkOperation[Call] = {
    case (offer, state) =>
      val offerCount = state.offerCounter.incrementAndGet
      val desiredRole = state.resourceRole
      val frameworkId = state.fwId
      val agentId = offer.getAgentId
      val ids = List(offer.getId)
      val resources = offer.getResourcesList.asScala.toList.groupBy(_.getName)
      val cpuList = resources.getOrElse("cpus", List[Resource]())
      val memList = resources.getOrElse("mem", List[Resource]())

      if (cpuList.nonEmpty && memList.nonEmpty && cpuList.size == memList.size) {
        var tasks: List[TaskInfo] = List()
        for ((cpu, mem) <- cpuList zip memList; if desiredRole == cpu.getRole && desiredRole == mem.getRole) {
          val cpusPerTask = state.cpusPerTask
          val memMbPerTask = state.memMbPerTask
          if (cpu.getScalar.getValue >= cpusPerTask && mem.getScalar.getValue >= memMbPerTask) {
            val taskId = s"task-$offerCount-${state.totalTaskCounter.incrementAndGet}"
            tasks ::= sleepTask(offer, agentId, taskId, cpu.getRole, cpusPerTask, mem.getRole, memMbPerTask)
          }
        }
        if (tasks.nonEmpty) {
          LOGGER.info("Launching {} tasks", tasks.size)
          sink(
            sleep(frameworkId, ids.asJava, tasks.asJava), { () => tasks.foreach { task => state.put(task.getTaskId, TaskState.TASK_STAGING) } }, { e: Throwable => LOGGER.warn("", e) })
        } else {
          sink(decline(frameworkId, ids.asJava))
        }
      } else {
        sink(decline(frameworkId, ids.asJava))
      }
  }

  private def sleep(frameworkId: Protos.FrameworkID, offerIds: util.List[Protos.OfferID], tasks: util.List[Protos.TaskInfo]): Call =
    Call.newBuilder
      .setFrameworkId(frameworkId)
      .setType(Call.Type.ACCEPT)
      .setAccept(
        Call.Accept.newBuilder
          .addAllOfferIds(offerIds)
          .addOperations(
            Offer.Operation.newBuilder
              .setType(Offer.Operation.Type.LAUNCH)
              .setLaunch(
                Offer.Operation.Launch.newBuilder
                  .addAllTaskInfos(tasks))))
      .build

  private def sleepTask(offer: Protos.Offer, agentId: Protos.AgentID, taskId: String, cpusRole: String, cpus: Double, memRole: String, mem: Double): TaskInfo =
    TaskHandler.createTaskWith(offer, DockerEntity("hello-world", "victorursan/akka-http-hello", DockerResource(0.2, 128.0)))

  /*
      TaskInfo.newBuilder
        .setName(taskId)
        .setTaskId(
          TaskID.newBuilder
            .setValue(taskId))
        .setAgentId(agentId)
          .setHealthCheck( HealthCheck.newBuilder())
          .setco
        .setCommand(
          CommandInfo.newBuilder
            .setEnvironment(
              Environment.newBuilder
                .addVariables(
                  Environment.Variable.newBuilder
                    .setName("SLEEP_SECONDS")
                    .setValue("15")))
            .setValue("env | sort && sleep $SLEEP_SECONDS"))
        .addResources(
          Resource.newBuilder
            .setName("cpus")
            .setRole(cpusRole)
            .setType(Value.Type.SCALAR)
            .setScalar(Value.Scalar.newBuilder.setValue(cpus)))
        .addResources(
          Resource.newBuilder
            .setName("mem")
            .setRole(memRole)
            .setType(Value.Type.SCALAR)
            .setScalar(
              Value.Scalar.newBuilder
                .setValue(mem)))
        .build
    */
}
