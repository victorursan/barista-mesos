package com.victorursan.utils

import org.apache.mesos.Protos.Offer

/**
 * Created by victor on 4/24/16.
 */
object BaristaSchedulerHelper {

  private def isGoodEnoughOffer(offer: Offer, dockerEntity: DockerEntity): Boolean =
    OfferHandler.getResource(offer, "cpus") > dockerEntity.resource.cpu && OfferHandler.getResource(offer, "mem") > dockerEntity.resource.mem

  def bestOfferForEntity(offers: List[Offer], dockerEntity: DockerEntity): Offer =
    offers.filter(isGoodEnoughOffer(_, dockerEntity)).sortBy(OfferHandler.getResource(_, "cpus")).head

}
