package com.victorursan.utils

import com.victorursan.state._
import org.specs2.mutable.Specification
import spray.json._

import scala.language.postfixOps

/**
  * Created by victor on 5/4/17.
  */
class JsonSupportTest extends Specification with JsonSupport {


  private val autoScalingJsStr =
    """ {  "algorithm": "static-threashold",
           "resource": "mem",
           "thresholds": {
              "load": [20, 60],
              "time": [10, 10],
              "cooldown": [30, 30],
              "boundaries": [1, 9]
              }
        }"""
  private val autoScaling = AutoScaling(algorithm = "static-threashold", resource = "mem", thresholds = Thresholds(List(20, 60), List(10, 10), List(30, 30), List(1, 9)))

  private val thresholdsJsStr =
    """{  "load" : [20, 60],
                                  		  "time" : [10, 10],
                                  		  "cooldown" : [30, 30],
                                  		  "boundaries" : [1, 9]
                                  	}"""
  private val threshold = Thresholds(List(20, 60), List(10, 10), List(30, 30), List(1, 9))
  private val packJsStr =
    """{
                            	"name": "pack3",
                            	"mix": [],
                              "autoScaling": {
                            	  "algorithm": "static-threashold",
                            	  "resource": "mem",
                            	  "thresholds": {
                            		  "load": [20, 60],
                            		  "time": [10, 10],
                            		  "cooldown": [30, 30],
                            		  "boundaries": [1, 9]
                            	}
                            }
                            }"""
  private val pack = Pack(name = "pack3", mix = Set(), autoScaling = AutoScaling(algorithm = "static-threashold", resource = "mem", thresholds = Thresholds(List(20, 60), List(10, 10), List(30, 30), List(1, 9))))

  private val upgradeBeanJsonStr =
    """{
        "beanId": "hello-world~68",
        "newBean": {
             "name": "hello-world-a",
             "dockerEntity": {
             "image": "victorursan/akka-http-hello",
             "role": "*",
             "resource": {
               "cpu": 0.3,
               "mem": 300.0,
              "ports": [ "4321" ]
           }
           },
             "checks": [
           {
             "httpPath": "/",
             "interval": 5
           }
             ]
           }
         }"""

  private val randomJsonStr =
    """{"taskId":"hello-world~2","name":"hello-world","dockerEntity":{"image":"victorursan/akka-http-hello",
    "role":"*", "network":"bridge", "resource":{"cpu":0.2,"mem":128.0, "ports":["4321", "4322 -> 1233"]}, "arguments":[]},
    "checks":[{"httpPath": "/", "interval": 5}],"hostname":"eec5810b-04f4-4dca-a75f-421ca290d920-O7653",
    "agentId":"eec5810b-04f4-4dca-a75f-421ca290d920-S1"}"""
  private val randomBean = Bean("2", "hello-world", DockerEntity("victorursan/akka-http-hello",
    resource = DockerResource(0.2, 128.0, disk = None, ports = List(DockerPort(4321, None), DockerPort(4322, Some(1233))))),
    checks = List(BeanCheck(httpPath = "/", interval = 5)),
    agentId = Some("eec5810b-04f4-4dca-a75f-421ca290d920-S1"), hostname = Some("eec5810b-04f4-4dca-a75f-421ca290d920-O7653"))

  private val dockerEntityJson =
    """ {"image":"victorursan/akka-http-hello", "role":"*","network":"host",
      |"resource":{"cpu":0.2,"mem":128.0, "disk":1233.0, "ports":[]},"arguments": []} """.stripMargin
  private val dockerEntity = DockerEntity(image = "victorursan/akka-http-hello", network = "host",
    resource = DockerResource(0.2, 128.0, Some(1233)), arguments = List())

  private val offerJson =
    """ { "id": "a", "agentId": "b", "hostname": "c", "mem": 1.1, "cpu": 2.2, "ports":[{"start": 1, "end": 4},
      |{"start": 9, "end": 1333}] } """.stripMargin
  private val offer = Offer(id = "a", agentId = "b", hostname = "c", mem = 1.1, cpu = 2.2, ports = List(Range.inclusive(1, 4),
    Range.inclusive(9, 1333)))


  "JsonSupportTest" should {
    "beanProtocol" in {
      randomJsonStr.parseJson.convertTo[Bean] must_== randomBean
      randomBean.toJson must_== randomJsonStr.parseJson
    }

    "dockerEntityProtocol" in {
      dockerEntityJson.parseJson.convertTo[DockerEntity] must_== dockerEntity
      dockerEntity.toJson must_== dockerEntityJson.parseJson
    }

    "offerProtocol" in {
      offerJson.parseJson.convertTo[Offer] must_== offer
      offer.toJson must_== offerJson.parseJson
    }

    "packProtocol" in {
      packJsStr.parseJson.convertTo[Pack] must_== pack
      pack.toJson must_== packJsStr.parseJson
    }

    "threshold" in {
      thresholdsJsStr.parseJson.convertTo[Thresholds] must_== threshold
      threshold.toJson must_== thresholdsJsStr.parseJson

    }

    "autoScaling" in {
      autoScalingJsStr.parseJson.convertTo[AutoScaling] must_== autoScaling
      autoScaling.toJson must_== autoScalingJsStr.parseJson
    }
  }
}
