package com.victorursan.utils

import org.apache.mesos.Protos.Offer
import org.slf4j.LoggerFactory

/**
 * Created by victor on 4/24/16.
 */
object BaristaSchedulerHelper {
  private val log = LoggerFactory.getLogger(BaristaSchedulerHelper.getClass)

  private def isGoodEnoughOffer(offer: Offer, dockerEntity: DockerEntity): Boolean = {
    log.info(s"isGoodEnoughOffer: offer: $offer, dockerEntity: $dockerEntity)")
    OfferHandler.getResource(offer, "cpus") > dockerEntity.resource.cpu && OfferHandler.getResource(offer, "mem") > dockerEntity.resource.mem
  }

  def bestOfferForEntity(offers: List[Offer], dockerEntity: DockerEntity): Offer = {
    log.info(s"bestOfferForEntity: offers: $offers, dockerEntity: $dockerEntity)")
    offers.filter(isGoodEnoughOffer(_, dockerEntity)).sortBy(OfferHandler.getResource(_, "cpus")).head
  }

}
