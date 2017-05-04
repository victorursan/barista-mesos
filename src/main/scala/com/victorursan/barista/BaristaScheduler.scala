package com.victorursan.barista

import com.victorursan.state.{Bean, ScheduleState}
import org.apache.mesos.v1.Protos.Offer

import scala.collection.JavaConverters._

/**
  * Created by victor on 5/3/17.
  */
object BaristaScheduler {

  def scheduleBeans(beans: Set[Bean], offers: List[Offer]): ScheduleState = {
    var remainningOffers = offers
    var acceptOffers = Set[Bean]()
    var scheduledBeans = Set[Bean]()
    for (bean <- beans) {
      scheduleBean(bean, remainningOffers).foreach(offer => {
        remainningOffers = remainningOffers.filterNot(_.equals(offer))
        scheduledBeans = scheduledBeans + bean
        acceptOffers = acceptOffers + bean.copy(agentId = Some(offer.getAgentId.getValue), offerId = Some(offer.getId.getValue))
      })
    }
    ScheduleState(acceptOffers, remainningOffers, scheduledBeans)
  }

  private def scheduleBean(bean: Bean, offers: List[Offer]): Option[Offer] =
    offers.find { offer => memFromOffer(offer) >= bean.dockerEntity.resource.mem && cpusFromOffer(offer) >= bean.dockerEntity.resource.cpu }

  private def memFromOffer(offer: Offer): Double =
    offer.getResourcesList.asScala.find(_.getName.equalsIgnoreCase("mem")).map(_.getScalar.getValue).getOrElse(0)

  private def cpusFromOffer(offer: Offer): Double =
    offer.getResourcesList.asScala.find(_.getName.equalsIgnoreCase("cpus")).map(_.getScalar.getValue).getOrElse(0)
}
