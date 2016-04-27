package com.victorursan.barista

import java.util

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import org.apache.mesos.Protos._
import org.apache.mesos.{ Scheduler, SchedulerDriver }
import rx.lang.scala.{ Subject, Subscriber }

import scala.collection.JavaConverters._

class BaristaScheduler extends Scheduler {
  protected def system: ActorSystem = ActorSystem()
  protected def log: LoggingAdapter = Logging(system, "BaristaScheduler")

  private val offersSubject = Subject[List[Offer]]()

  def subscribe(subscriber: Subscriber[List[Offer]]): Unit = offersSubject.subscribe(subscriber)

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
    offersSubject.onNext(offers.asScala.toList)
  }

  def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo): Unit = {
    log.info(s"re-registered")
  }

  def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo): Unit = {
    log.info(s"registered")
  }
}
