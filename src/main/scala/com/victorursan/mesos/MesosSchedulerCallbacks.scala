package com.victorursan.mesos

import org.apache.mesos.v1.Protos.{InverseOffer, Offer, OfferID, TaskStatus}
import org.apache.mesos.v1.scheduler.Protos.Event.{Error, Failure, Message, Subscribed}


/**
  * Created by victor on 4/10/17.
  */
trait MesosSchedulerCallbacks {
  def receivedSubscribed(subscribed: Subscribed): Unit

  def receivedOffers(offers: List[Offer]): Unit

  def receivedInverseOffers(offers: List[InverseOffer]): Unit

  def receivedRescind(offerId: OfferID): Unit

  def receivedRescindInverseOffer(offerId: OfferID): Unit

  def receivedUpdate(update: TaskStatus): Unit

  def receivedMessage(message: Message): Unit

  def receivedFailure(failure: Failure): Unit

  def receivedError(error: Error): Unit

  def receivedHeartbeat(): Unit
}
