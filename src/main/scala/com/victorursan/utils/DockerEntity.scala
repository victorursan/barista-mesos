package com.victorursan.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * Created by victor on 4/22/16.
 */

case class Resource(cpu: Double, mem: Double, port: Int)

case class DockerEntity(name: String, image: String, resource: Resource)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val resourceProtocol = jsonFormat3(Resource)
  implicit val dockerServiceProtocol = jsonFormat3(DockerEntity)
}
