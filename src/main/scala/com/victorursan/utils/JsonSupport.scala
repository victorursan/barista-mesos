package com.victorursan.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.victorursan.state.{Bean, DockerEntity, DockerResource}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * Created by victor on 4/2/17.
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val resourceProtocol: RootJsonFormat[DockerResource] = jsonFormat2(DockerResource)
  implicit val dockerServiceProtocol: RootJsonFormat[DockerEntity] = jsonFormat4(DockerEntity)
  implicit val beanProtocol: RootJsonFormat[Bean] = jsonFormat1(Bean)
}