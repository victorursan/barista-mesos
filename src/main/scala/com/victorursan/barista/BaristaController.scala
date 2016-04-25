package com.victorursan.barista

import com.victorursan.utils.{ BaristaSchedulerHelper, DockerEntity, TaskHandler }
import org.apache.mesos.Protos._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Promise }
import scala.util.{ Failure, Success }

/**
 * Created by victor on 4/25/16.
 */
class BaristaController {

  private val scheduler = new BaristaScheduler
  private val mesosMaster = System.getenv("ZK")
  private val driver = BaristaSchedulerDriver.newDriver(scheduler, mesosMaster)

  def start(): Unit = driver.start()

  def stop(): Unit = driver.stop()

  def offers(): Future[List[Offer]] = scheduler.offers

  def launchDockerEntity(dockerEntity: DockerEntity): Future[String] = {
    val promise = Promise[String]
    scheduler.offers.onComplete {
      case Success(offers) =>
        val offer: Offer = BaristaSchedulerHelper.bestOfferForEntity(offers, dockerEntity)
        val task: TaskInfo = TaskHandler.createTaskWith(offer, dockerEntity)
        launchTaskWithOffer(offer, task)
        promise.success(s"Trying to deploy: \n$dockerEntity")
      case Failure(error) => promise.failure(error)
    }
    promise.future
  }

  private def launchTaskWithOffer(offer: Offer, taskInfo: TaskInfo): Unit =
    driver.launchTasks(List(offer.getId).asJavaCollection, List(taskInfo).asJavaCollection)

}
