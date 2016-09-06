package com.victorursan.barista

import java.util

import org.apache.mesos.Protos._
import org.apache.mesos.{ Scheduler, SchedulerDriver }
import org.slf4j.LoggerFactory
import rx.lang.scala.{ Subject, Subscriber }

import scala.collection.JavaConverters._

class BaristaScheduler extends Scheduler {
  private val log = LoggerFactory.getLogger(classOf[BaristaScheduler])

  //  private val offersSubject = Subject[List[Offer]]()

  //  def subscribe(subscriber: Subscriber[List[Offer]]): Unit = offersSubject.subscribe(subscriber)

  def error(driver: SchedulerDriver, message: String): Unit = {
    log.info(s"error: driver: $driver, message: $message")
  }

  def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int): Unit = {
    log.info(s"executorLost: driver: $driver, executorId: $executorId, slaveId: $slaveId, status: $status")
  }

  def slaveLost(driver: SchedulerDriver, slaveId: SlaveID): Unit = {
    log.info(s"slaveLost: driver: $driver, slaveId: $slaveId")
  }

  def disconnected(driver: SchedulerDriver): Unit = {
    log.info(s"disconnected: driver: $driver")
  }

  def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]): Unit = {
    log.info(s"frameworkMessage: driver: $driver, executorId: $executorId, slaveId: $slaveId, data: $data")
  }

  def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    log.info(s"statusUpdate: driver: $driver, status: $status")
  }

  def offerRescinded(driver: SchedulerDriver, offerId: OfferID): Unit = {
    log.info(s"offerRescinded: driver: $driver, offerId: $offerId")
  }

  def resourceOffers(driver: SchedulerDriver, offers: util.List[Offer]): Unit = {
    log.info(s"resourceOffers: driver: $driver, offers: $offers")
    //    offersSubject.onNext(offers.asScala.toList)
  }

  def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo): Unit = {
    log.info(s"reregistered: driver: $driver, masterInfo: $masterInfo")
  }

  def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo): Unit = {
    log.info(s"registered: driver: $driver, frameworkId: $frameworkId, masterInfo: $masterInfo")
  }
}
