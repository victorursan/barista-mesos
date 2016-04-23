package com.victorursan.utils

import org.apache.mesos.Protos._
import spray.json.{ JsArray, JsNumber, JsObject, JsString }

import scala.collection.JavaConverters._

object JsonTransformer {
  def getJsonArray(offers: List[Offer]): JsArray = JsArray(offers.map(convertOffer).toVector)

  private def convertOffer(offer: Offer) = JsObject(
    "node_id" -> JsString(offer.getId.getValue),
    "node_ip" -> JsString(offer.getHostname),
    "cpus" -> JsNumber(offer.getResourcesList.asScala.find(proto => proto.getName == "cpus").get.getScalar.getValue),
    "memory" -> JsNumber(offer.getResourcesList.asScala.find(proto => proto.getName == "mem").get.getScalar.getValue),
    "disk" -> JsNumber(offer.getResourcesList.asScala.find(proto => proto.getName == "disk").get.getScalar.getValue))
}
