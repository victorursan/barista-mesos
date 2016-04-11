package com.victorursan

import java.lang.management.ManagementFactory

import akka.http.scaladsl.server.Directives._
import mesosphere.mesos.util.FrameworkInfo
import org.apache.mesos.MesosSchedulerDriver
import spray.json.JsArray

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

trait BaristaService extends BaseService {
  private val framework = FrameworkInfo("Barista")
  private val scheduler = new ScalaScheduler
  private val zk = sys.env("ZK")
  private val driver = new MesosSchedulerDriver(scheduler, framework.toProto, zk)
  driver.start()

  protected val serviceName = "Barista"
  protected val routes = pathPrefix("status") {
    get {
      log.info("/status executed")
      complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString))
    }
  } ~ pathPrefix("barista") {
    get {
      log.info("/barista executed")
      val offers = Await.result(scheduler.future, 10 second)
      val jsonOffer: JsArray = JsonTransformer getJsonArray offers
      complete(jsonOffer.prettyPrint)
    }
  }
}