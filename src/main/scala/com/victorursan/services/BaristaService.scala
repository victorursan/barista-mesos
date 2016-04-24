package com.victorursan.services

import java.lang.management.ManagementFactory

import com.victorursan.barista.{ BaristaScheduler, BaristaSchedulerDriver }
import com.victorursan.utils.{ DockerEntity, JsonTransformer, TaskHandler }
import org.apache.mesos.Protos._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

trait BaristaService extends BaseService {
  protected val serviceName = "BaristaService"
  protected val scheduler = new BaristaScheduler
  protected val mesosMaster = System.getenv("ZK")
  protected val driver = BaristaSchedulerDriver.newDriver(scheduler, mesosMaster)
  driver.start()
  protected val routes = pathPrefix("status") {
    get {
      log.info("/status executed")
      complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString))
    }
  } ~ path("offers") {
    get {
      log.info("/offers executed")
      onComplete(scheduler.future) {
        case Success(listOffer) => complete(JsonTransformer getJsonArray listOffer prettyPrint)
        case _                  => complete("Something went wrong")
      }
    }
  } ~ path("stop") {
    log.info("/stop executed")
    driver.stop()
    complete("stop barista")
  } ~ path("api" / "app") {
    log.info("/api/app executed")
    post {
      entity(as[DockerEntity]) { dockerImg =>
        scheduler.future.onComplete {
          case Success(offers) =>
            for (offer: Offer <- offers) {
              val task = TaskHandler.createTaskWith(offer, dockerImg)
              driver.launchTasks(List(offer.getId).asJavaCollection, List(task).asJavaCollection)
            }
          case _ => log.error("no offers")
        }
        complete(s"OK: $dockerImg")
      }
    }
  }

}