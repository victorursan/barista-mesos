package com.victorursan

import java.util

import org.apache.mesos.Protos._
import org.apache.mesos.{ Scheduler, SchedulerDriver }

import scala.collection.JavaConverters._
import scala.concurrent.Promise

class ScalaScheduler extends Scheduler {
  private val promise: Promise[List[Offer]] = Promise[List[Offer]]
  val future = promise.future

  def error(driver: SchedulerDriver, message: String) {}

  def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int) {}

  def slaveLost(driver: SchedulerDriver, slaveId: SlaveID) {}

  def disconnected(driver: SchedulerDriver) {}

  def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]) {}

  def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    println(s"received status update $status")
  }

  def offerRescinded(driver: SchedulerDriver, offerId: OfferID) {}

  def resourceOffers(driver: SchedulerDriver, offers: util.List[Offer]) {
    println(s"offers $offers")
    promise.trySuccess(offers.asScala.toList)
  }

  def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo) {}

  def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo) {}
}
