package com.victorursan.barista

import com.victorursan.mesos.MesosSchedulerCallbacks
import com.victorursan.zookeeper.StateController
import org.apache.mesos.v1.Protos.{InverseOffer, Offer, OfferID, TaskStatus}
import org.apache.mesos.v1.scheduler.Protos.Event.{Error, Failure, Message, Subscribed}

/**
  * Created by victor on 4/10/17.
  */
object BaristaCallbacks extends MesosSchedulerCallbacks {

  override def receivedSubscribed(subscribed: Subscribed): Unit = print(subscribed)

  override def receivedOffers(offers: List[Offer]): Unit = {
    val beans = StateController.awaitingBeans
    if (beans.nonEmpty) {
      val bean = beans.head
      val acceptableOffers = for (offer <- offers) yield offer

      BaristaCalls.acceptContainers(bean, acceptableOffers.map(_.getId), acceptableOffers.map(_.getAgentId))
      StateController.removeFromAccept(bean)

      val nonAcceptableOffers = offers diff acceptableOffers
      BaristaCalls.decline(nonAcceptableOffers.map(_.getId))
    } else {
      print(offers)
      BaristaCalls.decline(offers.map(_.getId))
    }
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
