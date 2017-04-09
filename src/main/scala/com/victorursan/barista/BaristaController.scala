package com.victorursan.barista

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
import rx.lang.scala.JavaConversions.{toJavaObservable, toScalaObservable}
import rx.lang.scala.Observable.from
import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.{Observable, Subject}

import scala.collection.JavaConverters._

/**
  * Created by victor on 4/2/17.
  */
class BaristaController {
  private val log = LoggerFactory.getLogger(classOf[BaristaController])
  private val fwName = "Barista"
  private val fwId = s"$fwName-${UUID.randomUUID}"
  private val mesosUri = URI.create("http://localhost:8000/mesos/api/v1/scheduler")
  private val role = "*"
  private val user = "root"
  private val stateObservable: Subject[State[Protos.FrameworkID, Protos.TaskID, Protos.TaskState]] = ReplaySubject.withSize(100)
  private val frameworkID = FrameworkID.newBuilder
    .setValue(fwId)
    .build()

  def start(): Unit = {
    val clientBuilder = ProtobufMesosClientBuilder
      .schedulerUsingProtos.mesosUri(mesosUri)
      .applicationUserAgentEntry(UserAgentEntries.literal("com.victorursan", "barista"))

    val subscribeCall: Call = SchedulerCalls.subscribe(
      frameworkID,
      Protos.FrameworkInfo.newBuilder()
        .setId(frameworkID)
        .setUser(user)
        .setName(fwName)
        .setFailoverTimeout(9)
        .setRole(role)
        .build())


    clientBuilder
      .subscribe(subscribeCall)
      .processStream { case (unicastEvents: rx.Observable[Event]) =>
        val events: Observable[Event] = toScalaObservable(unicastEvents.share())

        val offerEvaluations: Observable[Optional[SinkOperation[Call]]] =
          events.filter { (event: Event) => event.getType == Event.Type.OFFERS }
            .flatMap { (event: Event) => from(event.getOffers.getOffersList.asScala) }
            .zip(stateObservable)
            .map(t => Optional.of(handleOffer(t)))

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
              val ack = SchedulerCalls.ackUpdate(state.frameworkId, status.getUuid, status.getAgentId, status.getTaskId)
              Optional.of(SinkOperations.create(ack))
            }

        val errorLogger: Observable[Optional[SinkOperation[Call]]] =
          events.filter { event => event.getType == Event.Type.ERROR || (event.getType == Event.Type.UPDATE && event.getUpdate.getStatus.getState == TaskState.TASK_ERROR) }
            .doOnNext { _ => log.warn("Task Error: {}", ProtoUtils.protoToString _) }
            .map(_ => Optional.empty())

        toJavaObservable(
          offerEvaluations
            .merge(updateStatusAck)
            .merge(errorLogger)).asInstanceOf[rx.Observable[Optional[SinkOperation[Call]]]]
      }
      .build
      .openStream
  }

  def launchDockerEntity(dockerEntity: DockerEntity): String = {
    val stateObject = State[Protos.FrameworkID, Protos.TaskID, Protos.TaskState](frameworkID, dockerEntity, role)
    stateObservable.onNext(stateObject)
    ""
  }

  private def handleOffer: ((Protos.Offer, State[Protos.FrameworkID, Protos.TaskID, Protos.TaskState])) => SinkOperation[Call] = {
    case (offer, state) =>
      val desiredRole = state.resourceRole
      val frameworkId = state.frameworkId
      val agentId = offer.getAgentId
      val ids = List(offer.getId)
      val resources = offer.getResourcesList.asScala.toList.groupBy(_.getName)
      val cpuList = resources.getOrElse("cpus", List[Resource]())
      val memList = resources.getOrElse("mem", List[Resource]())

      if (cpuList.nonEmpty && memList.nonEmpty && cpuList.size == memList.size) {
        var tasks: List[TaskInfo] = List()
        for ((cpu, mem) <- cpuList zip memList; if desiredRole == cpu.getRole && desiredRole == mem.getRole; dockerEntity = state.dockerEntity) {
          if (cpu.getScalar.getValue >= dockerEntity.resource.cpu && mem.getScalar.getValue >= dockerEntity.resource.mem) {
            tasks ::= dockerTask(offer, dockerEntity)
          }
        }
        if (tasks.nonEmpty) {
          log.info("Launching {} tasks", tasks.size)
          sink(sleep(frameworkId, ids.asJava, tasks.asJava), { () => tasks.foreach { task => state.put(task.getTaskId, TaskState.TASK_STAGING) } }, { e: Throwable => log.warn("", e) })
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

  private def dockerTask(offer: Protos.Offer, dockerEntity: DockerEntity): TaskInfo = TaskHandler.createTaskWith(offer, dockerEntity)

  //  private val scheduler = new BaristaScheduler
  //  private val mesosMaster = System.getenv("ZK")
  //  private val driver = BaristaSchedulerDriver.newDriver(scheduler, mesosMaster)
  //
  //  private val resources: mutable.Map[String, Offer] = mutable.Map()
  //
  //  //  scheduler.subscribe(new Subscriber[List[Offer]]() {
  //  //    log.info(s"${classOf[BaristaController]} subscribed to scheduler: ${scheduler.getClass}")
  //  //    override def onNext(list: List[Offer]): Unit = list.foreach(offer => resources(offer.getSlaveId.getValue) = offer)
  //  //    override def onError(error: Throwable): Unit = log.error(s"subscriber: ${error.printStackTrace()}")
  //  //    override def onCompleted(): Unit = log.error(s"subscriber completed")
  //  //  })
  //
  //  def start(): Unit = {
  //    log.info(s"driver start")
  //    driver.start()
  //  }
  //
  //  def stop(): Unit = {
  //    log.info(s"driver stop")
  //    driver.stop()
  //  }
  //
  //  def offers(): List[Offer] = resources.values.toList
  //
  //  def launchDockerEntity(dockerEntity: DockerEntity): Status = {
  //    log.info(s"launchDockerEntity: dockerEntity: $dockerEntity")
  //    val offer: Offer = BaristaSchedulerHelper.bestOfferForEntity(resources.values.toList, dockerEntity)
  //    val task: TaskInfo = TaskHandler.createTaskWith(offer, dockerEntity)
  //    launchTaskWithOffer(offer, task)
  //  }
  //
  //  private def launchTaskWithOffer(offer: Offer, taskInfo: TaskInfo): Status = {
  //    log.info(s"launchTaskWithOffer: offer: $offer, taskInfo: $taskInfo")
  //    driver.launchTasks(List(offer.getId).asJavaCollection, List(taskInfo).asJavaCollection)
  //  }
  //
}
