package com.victorursan.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * Created by victor on 4/22/16.
 */

case class DockerResource(cpu: Double, mem: Double)

case class DockerEntity(name: String, image: String, resource: DockerResource)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val resourceProtocol = jsonFormat2(DockerResource)
  implicit val dockerServiceProtocol = jsonFormat3(DockerEntity)
}
