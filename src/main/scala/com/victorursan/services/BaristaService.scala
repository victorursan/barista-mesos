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

  protected val routes =
    path("status") {
      get {
        log.info("[GET] /status executed")
        complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString))
      }
    } ~ path("stop") {
      get {
        log.info("[GET] /stop executed")
        baristaController.stop()
        complete {
          System.exit(0)
          ""
        }
      }
    } ~ pathPrefix("api") {
      path("offers") {
        get {
          log.info("[GET] /api/offers executed")
          complete(JsonTransformer getJsonArray baristaController.offers() prettyPrint)
        }
      } ~ path("apps") {
        post {
          log.info("[POST] /api/apps executed")
          entity(as[DockerEntity]) { dockerEntity =>
            complete(baristaController.launchDockerEntity(dockerEntity).name())
          }
        }
      }
    }
}
