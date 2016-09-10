//package com.victorursan.utils
//
//import org.apache.mesos.Protos.Offer
//
//import scala.collection.JavaConverters._
//
///**
// * Created by victor on 4/24/16.
// */
//object OfferHandler {
//
//  def getResource(offer: Offer, name: String): Double = {
//    val resource = offer.getResourcesList.asScala.find(_.getName == name)
//    if (resource.isDefined) {
//      resource.get.getScalar.getValue
//    } else {
//      0
//    }
//  }
//
//}
