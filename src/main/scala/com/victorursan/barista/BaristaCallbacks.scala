package com.victorursan.barista

import com.victorursan.mesos.MesosSchedulerCallbacks
import com.victorursan.state.{Bean, ScheduleState}
import com.victorursan.utils.JsonSupport
import com.victorursan.zookeeper.StateController
import org.apache.mesos.v1.Protos._
import org.apache.mesos.v1.scheduler.Protos.Event.{Error, Failure, Message, Subscribed}

/**
  * Created by victor on 4/10/17.
  */
object BaristaCallbacks extends MesosSchedulerCallbacks with JsonSupport {

  override def receivedSubscribed(subscribed: Subscribed): Unit = print(subscribed)

  override def receivedOffers(offers: List[Offer]): Unit = {
    val beans = StateController.awaitingBeans
    val ScheduleState(scheduledBeans, canceledOffers, consumedBeans) = BaristaScheduler.scheduleBeans(beans, offers)
    scheduledBeans.foreach(BaristaCalls.acceptContainer(_))
    StateController.addToRunning(scheduledBeans)
    StateController.removeFromAccept(consumedBeans)

    BaristaCalls.decline(canceledOffers.map(_.getId))
  }

  override def receivedInverseOffers(offers: List[InverseOffer]): Unit = print(offers)

  override def receivedRescind(offerId: OfferID): Unit = print(offerId)

  override def receivedRescindInverseOffer(offerId: OfferID): Unit = print(offerId)

  override def receivedUpdate(update: TaskStatus): Unit = {
    val taskId = update.getTaskId.getValue
    val overview = StateController.addToOverview(taskId, update.toString)
    print(overview)
  }

  override def receivedMessage(message: Message): Unit = print(message)

  override def receivedFailure(failure: Failure): Unit = print(failure)

  override def receivedError(error: Error): Unit = print(error)

  override def receivedHeartbeat(): Unit = print("receivedHeartbeat")

}
