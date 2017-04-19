package com.victorursan.barista

import com.victorursan.mesos.MesosSchedulerCallbacks
import org.apache.mesos.v1.Protos.{InverseOffer, Offer, OfferID, TaskStatus}
import org.apache.mesos.v1.scheduler.Protos.Event.{Error, Failure, Message, Subscribed}

/**
  * Created by victor on 4/10/17.
  */
class BaristaCallbacks extends MesosSchedulerCallbacks {
  override def receivedSubscribed(subscribed: Subscribed): Unit = print(subscribed)

  override def receivedOffers(offers: List[Offer]): Unit = print(offers)

  override def receivedInverseOffers(offers: List[InverseOffer]): Unit = print(offers)

  override def receivedRescind(offerId: OfferID): Unit = print(offerId)

  override def receivedRescindInverseOffer(offerId: OfferID): Unit = print(offerId)

  override def receivedUpdate(update: TaskStatus): Unit = print(update)

  override def receivedMessage(message: Message): Unit = print(message)

  override def receivedFailure(failure: Failure): Unit = print(failure)

  override def receivedError(error: Error): Unit = print(error)

  override def receivedHeartbeat(): Unit = print("receivedHeartbeat")

}
