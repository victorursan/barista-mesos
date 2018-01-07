package com.victorursan.docker

import com.victorursan.state._
import com.victorursan.utils.JsonSupport
import spray.json._

/**
  * Created by victor on 6/6/17.
  */
object Main extends App with JsonSupport {
  private val upgradeBeanJsonStr =
    """{"beanId":"hello-world~68","newBean":{"name":"hello-world","dockerEntity":{"image":"victorursan/akka-http-hello","role":"*","resource":{"cpu":0.15,"mem":150.0,"ports":["4321"]}},"checks":[{"httpPath":"/","interval":5}]}}"""
  private val rawJsonStr =
    """{
      	"name": "hello-world",
      	"dockerEntity": {
      		"image": "victorursan/akka-http-hello",
      		"role": "*",
      		"resource": {
      			"cpu": 0.15,
      			"mem": 150.0,
      			"ports": [
      				"4321"
      			]
      		}
      	},
      	"checks": [
      		{
      			"httpPath": "/",
      			"interval": 5
      		}
      	]
      }"""
  //  val upgradeBean = UpgradeBean("hello-world~68", newBean = RawBean("hello-world-a", dockerEntity = DockerEntity("victorursan/akka-http-hello", resource = DockerResource(0.3,300,None,List(DockerPort(4321, None)))), checks = Some(List(BeanCheck("/", 5)))))
  println(rawJsonStr.parseJson.convertTo[RawBean])
  //  println(upgradeBean.toJson)
  //  val beanDocker = BeanDocker("pack1~hello-world~1", "d9d8504a249c1844f3c83da96481d8024d3d7c0d7eaebd084230ef8d7dab3b0e", "10.1.10.12")
  //  DockerController
  //    .registerBeanDocker(beanDocker)
  //    .subscribe(dockersta => println(dockersta))
  //  println("12312\n\n\n\n\n\n\n\n\nn\n\n\n\n\n")
}
