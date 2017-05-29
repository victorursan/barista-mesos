package com.victorursan.services

import java.lang.management.ManagementFactory

import akka.http.scaladsl.server.Route
import com.victorursan.barista.BaristaController
import com.victorursan.state.{Pack, RawBean, ScaleBean}
import com.victorursan.utils.Config
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by victor on 4/2/17.
  */
trait BaristaService extends BaseService with Config {
  protected val serviceName = "BaristaService"
  private val log = LoggerFactory.getLogger(classOf[BaristaService])
  private val baristaController: BaristaController = new BaristaController
  Future {
    baristaController.start()
  }

  val routes: Route =
    path("status") {
      get {
        log.info("[GET] /status executed")
        complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString))
      }
    } ~ path("stop") {
      post {
        log.info("[POST] /stop executed")
        complete {
          baristaController.teardown()
        }
      }
    } ~ pathPrefix("api") {
      pathPrefix("task") {
        pathPrefix("bean") {
          path("add") {
            log.info("[POST] /api/task/bean/add launching a new bean")
            entity(as[RawBean]) { rawBean: RawBean =>
              complete(baristaController.launchRawBean(rawBean))
            }
          } ~ path("scale") {
            log.info("[POST] /api/task/bean/scale scalling a bean")
            entity(as[ScaleBean]) { scaleBean: ScaleBean =>
              complete(baristaController.scaleBean(scaleBean))
            }
          }
        } ~ path("pack" / "add") {
          log.info("[POST] /api/task/pack/add launching a new pack")
          entity(as[Pack]) { pack: Pack =>
            complete(baristaController.launchPack(pack))
          }
        } ~ path("kill") {
          post {
            log.info("[POST] /api/task/kill killing the entity")
            entity(as[Set[String]]) { tasksId =>
              complete(baristaController.killTask(tasksId))
            } ~
            entity(as[String]) { tasksId =>
              complete(baristaController.killTask(Set(tasksId)))
            }
          }
        } ~ path("running" / "unpacked") {
          get {
            log.info("[GET] /api/task/running getting all tasks that should run")
            complete(baristaController.runningUnpackedTasks())
          }
        } ~ path("running" / "count") {
          get {
            log.info("[GET] /api/task/running/count getting all tasks that should run")
            complete(baristaController.runningTasksCount())
          }
        }
      } ~ path("overview") {
        get {
          log.info("[GET] /api/overview an overview")
          complete(baristaController.stateOverview())
        }
      } ~ pathPrefix("offers") {
        path("available") {
          get {
            log.info("[GET] /api/offers/available an overview of available offers")
            complete(baristaController.availableOffers)
          }
        }
      }
    }
}
