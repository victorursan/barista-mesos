package com.victorursan.services

import java.lang.management.ManagementFactory

import com.victorursan.barista.{ BaristaScheduler, BaristaSchedulerDriver }
import com.victorursan.utils.{ DockerEntity, JsonTransformer }

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

trait BaristaService extends BaseService {
  protected val serviceName = "BaristaService"
  protected val scheduler = new BaristaScheduler
  protected val mesosMaster = System.getenv("ZK")
  protected val runner = BaristaSchedulerDriver.newDriver(scheduler, mesosMaster)
  runner.start()
  protected val routes = pathPrefix("status") {
    get {
      log.info("/status executed")
      complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString))
    }
  } ~ path("barista") {
    get {
      log.info("/barista executed")
      onComplete(scheduler.future) {
        case Success(listOffer) => complete(JsonTransformer getJsonArray listOffer prettyPrint)
        case _                  => complete("Something went wrong")
      }
    }
  } ~ path("barista" / "stop") {
    log.info("/barista/stop executed")
    runner.stop()
    complete("stop barista")
  } ~ path("api" / "app") {
    log.info("/api/app executed")
    post {
      entity(as[DockerEntity]) { dockerImg =>
        complete(s"OK $dockerImg")
      }
    }
  }
}