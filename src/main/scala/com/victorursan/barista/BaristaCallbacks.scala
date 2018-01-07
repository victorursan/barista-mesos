package com.victorursan.barista

import com.victorursan.consul.{ServiceController, Utils}
import com.victorursan.docker.{DockerController, DockerStatus}
import com.victorursan.mesos.MesosSchedulerCallbacks
import com.victorursan.state.{AutoScaling, BeanDocker, Thresholds}
import com.victorursan.utils.JsonSupport
import com.victorursan.zookeeper.StateController
import org.apache.mesos.v1.Protos._
import org.apache.mesos.v1.scheduler.Protos.Call.Reconcile.Task
import org.apache.mesos.v1.scheduler.Protos.Event.{Error, Failure, Message, Subscribed}
import org.slf4j.LoggerFactory
import rx.lang.scala.subjects.PublishSubject
import rx.lang.scala.{Observable, Subject}

import scala.concurrent.duration._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

/**
  * Created by victor on 4/10/17.
  */
object BaristaCallbacks extends MesosSchedulerCallbacks with JsonSupport {

  private val monitorSubject: Subject[DockerStatus] = PublishSubject.apply[DockerStatus]()
  private val log = LoggerFactory.getLogger(BaristaCallbacks.getClass)

  private val statService: mutable.Map[String, mutable.Queue[(Long, DockerStatus)]] = mutable.TreeMap[String, mutable.Queue[(Long, DockerStatus)]]()
    .withDefault(_ => mutable.Queue[(Long, DockerStatus)]())
  private val packAutoscaling: mutable.Map[String, AutoScaling] = mutable.TreeMap[String, AutoScaling]()
    .withDefault(pack => StateController.getAutoScaling(pack))

  private val lastDeploy: mutable.Map[String, Long] = mutable.TreeMap().withDefault(_ => 0)
  private val timeOver: mutable.Map[String, Long] = mutable.TreeMap().withDefault(_ => 0)


  monitorSubject
    .groupBy(_.taskId.replaceAll("~(\\d)*$", ""))
    .foreach { case (taskName: String, tasks: Observable[DockerStatus]) =>
      tasks.groupBy(_.dateTime / 1000 * 1000)
        .foreach(task => {
          val queue: mutable.Queue[(Long, DockerStatus)] = statService(taskName)
          if (!queue.exists(_._1 == task._1)) {
            task._2.take(400 millis).reduce {(acc: DockerStatus, status: DockerStatus) =>
                DockerStatus(acc.taskId, dateTime = acc.dateTime, (acc.cpuPer + status.cpuPer) / 2, (acc.memPer + status.memPer) / 2, acc.memUsage + status.memUsage, acc.memAvailable + status.memAvailable)
            }.first.foreach((tasksss: DockerStatus) => {
              if (queue.lengthCompare(20) > 0) {
                while (queue.size > 20) queue.dequeue()
                queue.enqueue(task._1 -> tasksss)
              } else {
                queue.enqueue(task._1 -> tasksss)
              }
              statService.put(taskName, queue)
              if (queue.size > 19) {
                checkStateMonitor(taskName)
              }
            })
          }
        })
    }


  def checkStateMonitor(taskName: String): Unit = {
    val elements = statService(taskName).toList
    val elementsCount = elements.size
    val autoScaling = packAutoscaling(taskName.replaceAll("~.*$", ""))
    val sortedElements = elements.sortBy(_._1).reverse
    val result: Double = if (autoScaling.algorithm == "static-threashold") {
      if (autoScaling.resource == "mem") sortedElements.head._2.memPer else sortedElements.head._2.cpuPer
    } else sortedElements.map { case (_: Long, dockerStatus: DockerStatus) =>
      if (autoScaling.resource == "mem") dockerStatus.memPer else dockerStatus.cpuPer
    }
      .zipWithIndex.map { case (x: Double, i: Int) =>
      if (i != elementsCount - 1) 0.4 * math.pow(0.6, i) * x else math.pow(0.6, i) * x
    }.sum
    checkMonitoredValue(taskName, result, sortedElements.head._1 + 1000, autoScaling.thresholds)
  }

  def checkMonitoredValue(taskName: String, value: Double, time: Long, thresholds: Thresholds): Unit = {
    println(s"$taskName \t value:\t $value")
    val overTime = timeOver(taskName)
    if (thresholds.load.tail.head <= value) {
      if (overTime != 0) {
        val timeOverSeconds = (time - overTime) / 1000
        if (timeOverSeconds > thresholds.time.tail.head) {
          val lastDeploySeco = (time - lastDeploy(taskName)) / 1000
          if (lastDeploySeco > thresholds.cooldown.tail.head) {

            scaleUp(taskName, thresholds.boundaries)
            lastDeploy.put(taskName, time)
            timeOver.put(taskName, 0)
          }
        }
      } else {
        timeOver.put(taskName, time)
      }
    } else { // remove from timeOVer if any
      timeOver.put(taskName, 0)
    }
  }

  def scaleUp(taskName: String, boundaries: List[Int]): Unit =
    Future {
      val runningTasks = StateController.runningUnpacked.filter(_.taskId.replaceAll("~(\\d)*$", "").equalsIgnoreCase(taskName))
      val awaitingTasks = StateController.awaitingBeans.filter(_.taskId.replaceAll("~(\\d)*$", "").equalsIgnoreCase(taskName))
      println(taskName)
      if ((runningTasks.size + awaitingTasks.size) < boundaries.tail.head) {
        runningTasks.headOption.map(
          bean => BeanUtils.resetBean(bean)
        ).foreach(StateController.addToAccept)
      }
    }


  override def receivedSubscribed(subscribed: Subscribed): Unit = {
    print(subscribed.toString)
    BaristaCalls.reconsile(StateController.runningUnpacked.map(scheduledBean => {
      Task.newBuilder()
        .setTaskId(TaskID.newBuilder().setValue(scheduledBean.taskId).build())
        .setAgentId(AgentID.newBuilder().setValue(scheduledBean.agentId.get).build()) //todo fix scheduledBean.agentId.get
        .build()
    }))
  }

  override def receivedOffers(messosOffers: List[Offer]): Unit = {
    //    println("receivedOffers: " + messosOffers)
    MesosController.checkAgents(messosOffers.map(_.getAgentId))
    val offers = Utils.convertOffers(messosOffers)
    StateController.addToOffer(offers.toSet)
  }

  override def receivedInverseOffers(offers: List[InverseOffer]): Unit = println("receivedInverseOffers: " + offers)

  override def receivedRescind(offerId: OfferID): Unit = {
    println("receivedRescind: " + offerId)
    StateController.removeFromOffer(offerId.getValue)
  }

  override def receivedRescindInverseOffer(offerId: OfferID): Unit = print(offerId.toString)

  override def receivedUpdate(update: TaskStatus): Unit = {
    print(update.toString)
    if (update.hasTaskId && update.hasState) {
      val taskId = update.getTaskId.getValue
      update.getState match {
        case TaskState.TASK_LOST | TaskState.TASK_FAILED | TaskState.TASK_UNREACHABLE | TaskState.TASK_FINISHED =>
          StateController.runningUnpacked.find(s => s.taskId.equals(taskId)).foreach(scheduledBean => {
            ServiceController.deregisterService(scheduledBean.hostname.get, taskId) // todo
            StateController.addToAccept(scheduledBean) // todo
            StateController.removeRunningUnpacked(scheduledBean)
            StateController.removeFromBeanDocker(taskId)
          })
        case TaskState.TASK_KILLED =>
//          StateController.runningUnpacked.find(s => s.taskId.equals(taskId)).foreach(scheduledBean => {
            StateController.tasksToKill.find(t => t.equals(taskId)).foreach(StateController.removeFromKill)
//          })
        case TaskState.TASK_RUNNING =>
          StateController.runningUnpacked.find(s => s.taskId.equals(taskId)).foreach(scheduledBean => {
          ServiceController.setLoadBalancer("", BaristaController.loadBalancing)

            val baristaService = Utils.convertBeanToService(scheduledBean, scheduledBean.dockerEntity.resource.
              ports.headOption.map(_.hostPort.get).getOrElse(8500)) //todo
            val dockerId = s"mesos-${update.getAgentId.getValue}.${update.getContainerStatus.getContainerId.getValue}"
            val beanDocker = BeanDocker(scheduledBean.taskId, dockerId, scheduledBean.hostname.get)
            StateController.addToBeanDocker(beanDocker)
            //            monitorSubject.ad
            DockerController
              .registerBeanDocker(beanDocker)
              .subscribe((dockerStatus: DockerStatus) =>
                monitorSubject.onNext(dockerStatus)
                //                println(dockerStatus)
              )
            ServiceController.registerService(baristaService.serviceAddress, baristaService) // todo
          })
        case e => println(s"it's something $e \n")
      }
      print(StateController.addToOverview(taskId, update.toString).toString())
    }
  }

  override def receivedMessage(message: Message): Unit = log.info("receivedMessage", message)

  override def receivedFailure(failure: Failure): Unit = log.error("receivedError", failure) //todo delete from agentresources 

  override def receivedError(error: Error): Unit = log.error("receivedError", error)

  override def receivedHeartbeat(): Unit = log.debug("receivedHeartbeat")

}
