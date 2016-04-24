package com.victorursan.utils

import org.apache.mesos.Protos._
import spray.json.{ JsArray, JsNumber, JsObject, JsString, JsValue }

import scala.collection.JavaConverters._

object JsonTransformer {
  def getJsonArray(offers: List[Offer]): JsArray = JsArray(offers.map(convertOffer).toVector)

  private def convertOffer(offer: Offer): JsValue = JsObject(
    "node_id" -> JsString(offer.getId.getValue),
    "node_ip" -> JsString(offer.getHostname),
    "cpus" -> JsNumber(getResource(offer, "cpus")),
    "memory" -> JsNumber(getResource(offer, "mem")),
    "disk" -> JsNumber(getResource(offer, "disk")))

  private def getResource(offer: Offer, name: String): Double = {
    val resource = offer.getResourcesList.asScala.find(_.getName == "cpus")
    if (resource.isDefined) {
      resource.get.getScalar.getValue
    } else {
      0
    }
  }

}
