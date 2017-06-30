package com.victorursan.barista

import com.victorursan.consul.{ServiceController, Utils}
import com.victorursan.docker.DockerController
import com.victorursan.mesos.MesosSchedulerCallbacks
import com.victorursan.state.BeanDocker
import com.victorursan.utils.JsonSupport
import com.victorursan.zookeeper.StateController
import org.apache.mesos.v1.Protos._
import org.apache.mesos.v1.scheduler.Protos.Call.Reconcile.Task
import org.apache.mesos.v1.scheduler.Protos.Event.{Error, Failure, Message, Subscribed}
import org.slf4j.LoggerFactory

/**
  * Created by victor on 4/10/17.
  */
object BaristaCallbacks extends MesosSchedulerCallbacks with JsonSupport {
  private val log = LoggerFactory.getLogger(BaristaCallbacks.getClass)

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
    println("receivedOffers: " + messosOffers)
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
        case TaskState.TASK_LOST | TaskState.TASK_FAILED | TaskState.TASK_UNREACHABLE =>
          StateController.runningUnpacked.find(s => s.taskId.equals(taskId)).foreach(scheduledBean => {
            ServiceController.deregisterService(scheduledBean.hostname.get, taskId) // todo
            StateController.addToAccept(scheduledBean) // todo
            StateController.removeRunningUnpacked(scheduledBean)
            StateController.removeFromBeanDocker(taskId)
          })
        case TaskState.TASK_KILLED =>
          StateController.runningUnpacked.find(s => s.taskId.equals(taskId)).foreach(scheduledBean => {
            ServiceController.deregisterService(scheduledBean.hostname.get, taskId) // todo
            StateController.removeRunningUnpacked(scheduledBean)
            StateController.tasksToKill.find(t => t.equals(taskId)).foreach(StateController.removeFromKill)
            StateController.removeFromBeanDocker(taskId)
          })
        case TaskState.TASK_RUNNING =>
          StateController.runningUnpacked.find(s => s.taskId.equals(taskId)).foreach(scheduledBean => {

            val baristaService = Utils.convertBeanToService(scheduledBean, scheduledBean.dockerEntity.resource.
              ports.headOption.map(_.hostPort.get).getOrElse(8500)) //todo
            update.getData.toStringUtf8.split("\"Id\": \"").toList // todo hardcoded, we are looking after the docker id
              .tail.headOption
              .map(_.takeWhile(_ != '"'))
              .foreach(dockerId => {
                val beanDocker = BeanDocker(scheduledBean.taskId, dockerId, scheduledBean.hostname.get)
                StateController.addToBeanDocker(beanDocker)
                DockerController
                  .registerBeanDocker(beanDocker)
                  .subscribe(dockerista => Unit
//todo                    println(dockerista)
                  )
                })
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
