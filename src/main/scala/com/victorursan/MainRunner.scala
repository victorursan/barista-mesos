package com.victorursan

import org.apache.mesos.{ MesosSchedulerDriver, Protos }
import org.apache.mesos.Protos.FrameworkInfo

/**
 * Created by victor on 4/11/16.
 */
object MainRunner {
  val scalaScheduler = new ScalaScheduler
  val mesosMaster = sys.env("ZK")
  val frameworkInfo = FrameworkInfo.newBuilder()
    .setUser("") // Have Mesos fill in the current user.
    .setName("Barista")
    .setCheckpoint(true)
    .build()

  def runFramework(): Protos.Status = {
    val driver = new MesosSchedulerDriver(scalaScheduler, frameworkInfo, mesosMaster)
    driver.start
  }
}
