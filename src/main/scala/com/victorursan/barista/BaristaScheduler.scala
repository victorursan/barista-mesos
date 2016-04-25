package com.victorursan.barista

import java.util

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import org.apache.mesos.Protos._
import org.apache.mesos.{ Scheduler, SchedulerDriver }

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BaristaScheduler extends Scheduler {
  protected def system: ActorSystem = ActorSystem()
  protected def log: LoggingAdapter = Logging(system, "BaristaScheduler")

  var offers: Future[List[Offer]] = Future {
    List()
  }

  def error(driver: SchedulerDriver, message: String): Unit = {
    log.info(s"error $message")
  }

  def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int): Unit = {
    log.info(s"executorLost")
  }

  def slaveLost(driver: SchedulerDriver, slaveId: SlaveID): Unit = {
    log.info(s"slaveLost")
  }

  def disconnected(driver: SchedulerDriver): Unit = {
    log.info(s"disconnected")
  }

  def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]): Unit = {
    log.info(s"frameworkMessage")
  }

  def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    log.info(s"statusUpdate \n $status")
  }

  def offerRescinded(driver: SchedulerDriver, offerId: OfferID): Unit = {
    log.info(s"offerRescinded")
  }

  def resourceOffers(driver: SchedulerDriver, offers: util.List[Offer]): Unit = {
    log.info(s"resourceOffers $offers")
    this.offers = Future { offers.asScala.toList }
  }

  def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo): Unit = {
    log.info(s"re-registered")
  }

  def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo): Unit = {
    log.info(s"registered")
  }
}
