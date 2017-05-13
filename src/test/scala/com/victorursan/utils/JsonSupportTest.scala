package com.victorursan.utils

import com.victorursan.state.{Bean, DockerEntity, DockerPort, DockerResource}
import org.specs2.mutable.Specification
import spray.json._

import scala.language.postfixOps

/**
  * Created by victor on 5/4/17.
  */
class JsonSupportTest extends Specification with JsonSupport {
  private val randomJsonStr = """{"taskId":"hello-world~2","name":"hello-world","dockerEntity":{"image":"victorursan/akka-http-hello",
    "role":"*", "network":"bridge", "resource":{"cpu":0.2,"mem":128.0, "ports":["4321", "4322 -> 1233"]}, "arguments":[]},"hostname":"eec5810b-04f4-4dca-a75f-421ca290d920-O7653",
    "agentId":"eec5810b-04f4-4dca-a75f-421ca290d920-S1"}"""
  private val randomBean = Bean("2", "hello-world", DockerEntity("victorursan/akka-http-hello",
    resource=DockerResource(0.2, 128.0, disk = None, ports = List(DockerPort(4321, None),DockerPort(4322, Some(1233))))),
    None, Some("eec5810b-04f4-4dca-a75f-421ca290d920-S1"), Some("eec5810b-04f4-4dca-a75f-421ca290d920-O7653"))

  private val dockerEntityJson = """ {"image":"victorursan/akka-http-hello", "role":"*","network":"host","resource":{"cpu":0.2,"mem":128.0, "disk":1233.0, "ports":[]},"arguments": []} """
  private val dockerEntity = DockerEntity(image = "victorursan/akka-http-hello", network="host", resource = DockerResource(0.2, 128.0, Some(1233)), arguments=List())

  "JsonSupportTest" should {
    "beanProtocol" in {
      randomJsonStr.parseJson.convertTo[Bean] must_== randomBean
      randomBean.toJson must_== randomJsonStr.parseJson
    }

    "dockerEntityProtocol" in {
      dockerEntityJson.parseJson.convertTo[DockerEntity] must_== dockerEntity
      dockerEntity.toJson must_== dockerEntityJson.parseJson
    }

  }
}
