package com.victorursan.services

import java.lang.management.ManagementFactory

import akka.http.scaladsl.model.{ HttpHeader, HttpResponse, MediaTypes }
import com.victorursan.barista.BaristaScheduler

import scala.concurrent.ExecutionContext.Implicits.global
//import com.victorursan.barista.BaristaController
import com.victorursan.mesos.Leader
import akka.http.scaladsl.model.headers._
//import com.victorursan.utils.{ DockerEntity, JsonTransformer }
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

trait BaristaService extends BaseService {
  private val log = LoggerFactory.getLogger(classOf[BaristaService])
  protected val serviceName = "BaristaService"
  //  private val baristaController: BaristaController = new BaristaController
  //  baristaController.start()

  protected val routes =
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
    } ~ path("leader") {

      complete {
        Future {
          Leader.get()
            .flatMap { uri => Future { uri.path.toString() } }
        }
      }
    }
  //  ~ pathPrefix("api") {
  //      path("cluster-resources") {
  //        get {
  //          log.info("[GET] /api/cluster-resources executed")
  ////          complete(JsonTransformer getJsonArray baristaController.offers() prettyPrint)
  //        }
  //      } ~ path("services") {
  //        post {
  //          log.info("[POST] /api/services executed")
  //          entity(as[DockerEntity]) { dockerEntity =>
  ////            complete(baristaController.launchDockerEntity(dockerEntity).name())
  //          }
  //        }
  //      }
  //    }
}
