package com.victorursan.utils

import com.victorursan.state.{Bean, DockerEntity, DockerResource}
import org.specs2.mutable.Specification
import spray.json._
import scala.language.postfixOps

/**
  * Created by victor on 5/4/17.
  */
class JsonSupportTest extends Specification with JsonSupport {
  private val randomJsonStr = """{"taskId":"hello-world~2","name":"hello-world","dockerEntity":{"image":"victorursan/akka-http-hello",
    "role":"*","resource":{"cpu":0.2,"mem":128.0}},"offerId":"eec5810b-04f4-4dca-a75f-421ca290d920-O7653",
    "agentId":"eec5810b-04f4-4dca-a75f-421ca290d920-S1"}"""
  private val randomBean = Bean("2", "hello-world", DockerEntity("victorursan/akka-http-hello", "*", DockerResource(0.2, 128.0)),
    None, Some("eec5810b-04f4-4dca-a75f-421ca290d920-S1"), Some("eec5810b-04f4-4dca-a75f-421ca290d920-O7653"))

  "JsonSupportTest" should {
    "beanProtocol" in {
      randomJsonStr.parseJson.convertTo[Bean] must_== randomBean
      randomBean.toJson must_== randomJsonStr.parseJson
    }

  }
}
