package com.victorursan.barista

import java.util.Optional

import com.mesosphere.mesos.rx.java.SinkOperation
import com.victorursan.mesos.MesosSchedulerCallbacks
import com.victorursan.state.DockerEntity
import org.apache.mesos.v1.Protos.{InverseOffer, Offer, OfferID, TaskStatus}
import org.apache.mesos.v1.scheduler.Protos.Call
import org.apache.mesos.v1.scheduler.Protos.Call.Type.ACCEPT
import org.apache.mesos.v1.scheduler.Protos.Event.{Error, Failure, Message, Subscribed}
import rx.subjects.SerializedSubject

import scala.compat.java8.OptionConverters.toScala

/**
  * Created by victor on 4/10/17.
  */
class BaristaCallbacks(obj: SerializedSubject[Optional[SinkOperation[Call]], Optional[SinkOperation[Call]]], calls: BaristaCalls) extends MesosSchedulerCallbacks {
  private var toAccept: List[DockerEntity] = List()
  private var toDecline = List()

  obj.subscribe(optAct => {
    toScala(optAct) match {
      case Some(offer) if offer.getThingToSink.getType == ACCEPT => toAccept
      case _ => print(_)
    }
  })

  override def receivedSubscribed(subscribed: Subscribed): Unit = print(subscribed)

  override def receivedOffers(offers: List[Offer]): Unit = {
    if (toAccept.nonEmpty) {
      calls.acceptContainer(toAccept.head, offers.map(_.getId), offers.head.getAgentId)
    } else {
      print(offers)
      calls.decline(offers.map(_.getId))
    }
  }

  override def receivedInverseOffers(offers: List[InverseOffer]): Unit = print(offers)

  override def receivedRescind(offerId: OfferID): Unit = print(offerId)

  override def receivedRescindInverseOffer(offerId: OfferID): Unit = print(offerId)

  override def receivedUpdate(update: TaskStatus): Unit = print(update)

  override def receivedMessage(message: Message): Unit = print(message)

  override def receivedFailure(failure: Failure): Unit = print(failure)

  override def receivedError(error: Error): Unit = print(error)

  override def receivedHeartbeat(): Unit = print("receivedHeartbeat")

}
