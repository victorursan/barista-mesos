package com.victorursan.barista

import org.apache.mesos.Protos.{ FrameworkID, FrameworkInfo }
import org.apache.mesos.{ MesosSchedulerDriver, SchedulerDriver }

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.sys.SystemProperties

/**
 * Created by victor on 4/16/16.
 */
object BaristaSchedulerDriver {
  //  private[this] val log = LoggerFactory.getLogger(getClass)

  def newDriver(newScheduler: BaristaScheduler,
    mesosMaster: String): SchedulerDriver = {
    val userName = new SystemProperties().get("user.name").getOrElse("")
    //    log.info(s"Create new Scheduler Driver with frameworkId: $frameworkId")
    val frameworkInfoBuilder = FrameworkInfo.newBuilder()
      .setName("Barista")
      .setFailoverTimeout(100000)
      .setUser(userName)
      .setCheckpoint(true)
      .setHostname(java.net.InetAddress.getLocalHost.getHostName)

    val implicitAcknowledgements = false

    new MesosSchedulerDriver(newScheduler, frameworkInfoBuilder.build(), mesosMaster, implicitAcknowledgements)
  }
}
