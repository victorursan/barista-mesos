package com.victorursan.barista

import com.victorursan.mesos.MesosSchedulerCallbacks
import com.victorursan.state.ScheduleState
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
    log.info(subscribed.toString)
    BaristaCalls.reconsile(StateController.running.map(scheduledBean => {
      Task.newBuilder()
        .setTaskId(TaskID.newBuilder().setValue(scheduledBean.taskId).build())
        .setAgentId(AgentID.newBuilder().setValue(scheduledBean.agentId.get).build()) //todo fix scheduledBean.agentId.get
        .build()
    }))
  }

  override def receivedOffers(offers: List[Offer]): Unit = {
    val beans = StateController.awaitingBeans
    val ScheduleState(scheduledBeans, canceledOffers, consumedBeans) = BaristaScheduler.scheduleBeans(beans, offers)

    scheduledBeans.foreach(BaristaCalls.acceptContainer(_))
    StateController.addToRunning(scheduledBeans)
    StateController.removeFromAccept(consumedBeans)

    BaristaCalls.decline(canceledOffers.map(_.getId))
  }

  override def receivedInverseOffers(offers: List[InverseOffer]): Unit = log.info(offers.toString())

  override def receivedRescind(offerId: OfferID): Unit = log.info(offerId.toString)

  override def receivedRescindInverseOffer(offerId: OfferID): Unit = log.info(offerId.toString)

  override def receivedUpdate(update: TaskStatus): Unit = {
    log.info(update.toString)
    val runningTasks = StateController.running
    if (update.hasTaskId && update.hasState) {
      val taskId = update.getTaskId.getValue
      update.getState match {
        case TaskState.TASK_LOST | TaskState.TASK_FAILED | TaskState.TASK_UNREACHABLE =>
          runningTasks.find(s => s.taskId.equals(taskId)).foreach(scheduledBean => {
            StateController.addToAccept(scheduledBean) // todo
            StateController.removeRunning(scheduledBean)
          })
        case _ => log.info("something \n\n\n\n\n\n\n\n\n")
      }
      log.info(StateController.addToOverview(taskId, update.toString).toString())
    }
  }

  override def receivedMessage(message: Message): Unit = print(message)

  override def receivedFailure(failure: Failure): Unit = print(failure)

  override def receivedError(error: Error): Unit = print(error)

  override def receivedHeartbeat(): Unit = print("receivedHeartbeat")

}
