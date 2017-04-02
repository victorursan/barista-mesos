package com.victorursan.services

import java.lang.management.ManagementFactory

import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by victor on 4/2/17.
  */
trait BaristaService extends BaseService{
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
