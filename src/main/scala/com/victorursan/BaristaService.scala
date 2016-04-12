package com.victorursan

import java.lang.management.ManagementFactory

import akka.http.scaladsl.server.Directives._
import spray.json.JsArray

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

trait BaristaService extends BaseService {
  protected val serviceName = "BaristaService"
  MainRunner.runFramework()

  protected val routes = pathPrefix("status") {
    get {
      log.info("/status executed")
      complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString))
    }
  } ~ pathPrefix("barista") {
    get {
      log.info("/barista executed")
      val offers = Await.result(MainRunner.scalaScheduler.future, 10 second)
      val jsonOffer: JsArray = JsonTransformer getJsonArray offers
      complete(jsonOffer.prettyPrint)
    }
  }
}