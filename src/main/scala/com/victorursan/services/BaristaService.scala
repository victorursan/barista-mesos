package com.victorursan.services

import java.lang.management.ManagementFactory

import com.victorursan.barista.{BaristaController, DockerEntity}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps

/**
  * Created by victor on 4/2/17.
  */
trait BaristaService extends BaseService {
  private val log = LoggerFactory.getLogger(classOf[BaristaService])
  private val baristaController: BaristaController = new BaristaController
  protected val serviceName = "BaristaService"

  Future {
    baristaController.start()
  }

  val routes =
    path("status") {
      get {
        log.info("[GET] /status executed")

        complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString))
      }
    } ~ path("stop") {
      post {
        log.info("[POST] /stop executed")
        //        baristaController.stop()

        complete {
          System.exit(0)
          ""
        }
      }
    } ~ pathPrefix("api") {
      path("task") {
        post {
          log.info("[POST] /api/task launching a new entity")
          entity(as[DockerEntity]) { dockerEntity =>
            complete(baristaController.launchDockerEntity(dockerEntity))
          }
        }
      }
    }
  //  ~ path("leader") {
  //
  //      complete {
  //        Future {
  //          Leader.get()
  //            .flatMap { uri => Future { uri.path.toString() } }
  //        }
  //      }
  //    }
}
