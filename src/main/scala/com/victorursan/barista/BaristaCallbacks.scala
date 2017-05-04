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
    print(subscribed.toString)
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

  override def receivedInverseOffers(offers: List[InverseOffer]): Unit = print(offers.toString())

  override def receivedRescind(offerId: OfferID): Unit = print(offerId.toString)

  override def receivedRescindInverseOffer(offerId: OfferID): Unit = print(offerId.toString)

  override def receivedUpdate(update: TaskStatus): Unit = {
    print(update.toString)
    if (update.hasTaskId && update.hasState) {
      val taskId = update.getTaskId.getValue
      update.getState match {
        case TaskState.TASK_LOST | TaskState.TASK_FAILED | TaskState.TASK_UNREACHABLE =>
          StateController.running.find(s => s.taskId.equals(taskId)).foreach(scheduledBean => {
            StateController.addToAccept(scheduledBean) // todo
            StateController.removeRunning(scheduledBean)
          })
        case TaskState.TASK_KILLED =>
          StateController.tasksToKill.find(t => t.getValue.equals(taskId)).foreach(StateController.removeFromKill)
        case _ => print("something \n\n\n\n\n\n\n\n\n")
      }
      print(StateController.addToOverview(taskId, update.toString).toString())
    }
  }

  override def receivedMessage(message: Message): Unit = print(message)

  override def receivedFailure(failure: Failure): Unit = print(failure)

  override def receivedError(error: Error): Unit = print(error)

  override def receivedHeartbeat(): Unit = print("receivedHeartbeat")

}
