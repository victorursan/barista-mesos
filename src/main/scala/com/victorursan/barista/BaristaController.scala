package com.victorursan.barista

import java.net.URI
import java.util.UUID

import com.mesosphere.mesos.rx.java.util.UserAgentEntries
import com.victorursan.state.DockerEntity
import com.victorursan.zookeeper.StateController

/**
  * Created by victor on 4/2/17.
  */
class BaristaController {
  private val fwName = "Barista"
  private val fwId = s"$fwName-${UUID.randomUUID}"
  private val mesosUri = URI.create("http://localhost:8000/mesos/api/v1/scheduler")
  private val role = "*"

  def start(): Unit = {
    BaristaCalls.subscribe(mesosUri, fwName, 10, role, UserAgentEntries.literal("com.victorursan", "barista"), fwId)
  }

  def launchDockerEntity(dockerEntity: DockerEntity): String = {
    StateController.addToAccept(dockerEntity) mkString ","
  }
}
