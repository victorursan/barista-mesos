package com.victorursan.barista

import java.net.URI
import java.util.UUID

import com.mesosphere.mesos.rx.java.util.UserAgentEntries
import com.victorursan.state.{Bean, DockerEntity, RawBean}
import com.victorursan.utils.JsonSupport
import com.victorursan.zookeeper.StateController
import org.apache.mesos.v1.Protos.TaskID
import spray.json._

import scala.language.postfixOps

/**
  * Created by victor on 4/2/17.
  */
class BaristaController extends JsonSupport {
  private val fwName = "Barista"
  private val fwId = s"$fwName-${UUID.randomUUID}"
  private val mesosUri = URI.create("http://localhost:8000/mesos/api/v1/scheduler")
  private val role = "*"

  def start(): Unit =
    BaristaCalls.subscribe(mesosUri, fwName, 10, role, UserAgentEntries.literal("com.victorursan", "barista"), fwId)

  def launchRawBean(rawBean: RawBean): JsValue = {
    val taskId = StateController.getNextId
    StateController.addToAccept(rawBean.toBean(taskId)) toJson
  }

  def stateOverview(): String =
    StateController.getOverview mkString ","

  def runningTasks(): JsValue =
    StateController.running.toJson

  def killTask(taskId: String): JsValue  = {
    StateController.running.find(_.taskId.equalsIgnoreCase(taskId)).foreach(StateController.removeRunning)
    val tasks = StateController.addToKill(TaskID.newBuilder().setValue(taskId).build())
    for(task <- tasks) {
      BaristaCalls.kill(task)
    }
    tasks.map(_.getValue) toJson
  }

  def teardown(): String  = {
    BaristaCalls.teardown()
    "We are closed"
  }
}
