//package com.victorursan.utils
//
//import org.apache.mesos.Protos.Offer
//import spray.json.{ JsArray, JsNumber, JsObject, JsString, JsValue }
//
//object JsonTransformer {
//  def getJsonArray(offers: List[Offer]): JsArray = JsArray(offers.map(convertOffer).toVector)
//
//  private def convertOffer(offer: Offer): JsValue = JsObject(
//    "node_id" -> JsString(offer.getId.getValue),
//    "node_ip" -> JsString(offer.getHostname),
//    "cpus" -> JsNumber(OfferHandler.getResource(offer, "cpus")),
//    "memory" -> JsNumber(OfferHandler.getResource(offer, "mem")),
//    "disk" -> JsNumber(OfferHandler.getResource(offer, "disk")))
//
//}
