package com.victorursan.services

import java.lang.management.ManagementFactory

import com.victorursan.barista.BaristaController
import com.victorursan.utils.{ DockerEntity, JsonTransformer }

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

trait BaristaService extends BaseService {
  protected val serviceName = "BaristaService"
  private val baristaController: BaristaController = new BaristaController
  baristaController.start()

  protected val routes = pathPrefix("status") {
    get {
      log.info("/status executed")
      complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString))
    }
  } ~ path("offers") {
    get {
      log.info("/offers executed")
      onComplete(baristaController.offers()) {
        case Success(listOffer) => complete(JsonTransformer getJsonArray listOffer prettyPrint)
        case Failure(error)     => complete(error)
      }
    }
  } ~ path("stop") {
    log.info("/stop executed")
    baristaController.stop()
    complete {
      System.exit(0)
      "stop barista"
    }
  } ~ path("api" / "app") {
    log.info("/api/app executed")
    post {
      entity(as[DockerEntity]) { dockerEntity =>
        onComplete(baristaController.launchDockerEntity(dockerEntity)) {
          case Success(result) => complete(result)
          case Failure(error)  => complete(error)
        }
      }
    }
  }
}