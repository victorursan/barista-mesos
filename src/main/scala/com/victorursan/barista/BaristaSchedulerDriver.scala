package com.victorursan.barista

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import org.apache.mesos.Protos.FrameworkInfo
import org.apache.mesos.{ MesosSchedulerDriver, SchedulerDriver }
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.sys.SystemProperties

/**
 * Created by victor on 4/16/16.
 */
object BaristaSchedulerDriver {
  protected def system: ActorSystem = ActorSystem()
  protected def log: LoggingAdapter = Logging(system, "BaristaSchedulerDriver")

  def newDriver(newScheduler: BaristaScheduler, mesosMaster: String): SchedulerDriver = {

    val userName = new SystemProperties().get("user.name").getOrElse("")

    val frameworkInfo = FrameworkInfo.newBuilder()
      .setName("Barista")
      .setFailoverTimeout((10 minutes) toMillis)
      .setUser(userName)
      .setCheckpoint(true)
      .setHostname(java.net.InetAddress.getLocalHost.getHostName)
      .build()

    log.info(s"Create new Scheduler Driver with frameworkId: ${frameworkInfo.getId}")
    val implicitAcknowledgements = false

    new MesosSchedulerDriver(newScheduler, frameworkInfo, mesosMaster, implicitAcknowledgements)
  }
}
