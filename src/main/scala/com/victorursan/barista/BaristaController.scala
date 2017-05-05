package com.victorursan.barista

import java.net.URI
import java.util.UUID

import com.mesosphere.mesos.rx.java.util.UserAgentEntries
import com.victorursan.state.{Bean, Pack, QuantityBean, RawBean}
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

  def launchPack(pack: Pack): JsValue = {
    val newPack = pack.copy(mix = pack.mix.map(qb => qb.copy(bean = qb.bean.copy(pack = Some(pack.name))))) //set pack name for all rawBeans
    var toLaunch: Set[Bean] = Set()
    val newMix: Set[QuantityBean] = newPack.mix.map(qb => {
      val beans = 1.to(qb.quantity).map(_ => StateController.getNextId).map(id => qb.bean.toBean(id))
      toLaunch = toLaunch ++ beans
      val taskIds = beans.map(_.taskId).toSet
      qb.copy(taskIds = Some(taskIds))
    })
    StateController.addToAccept(toLaunch)
    newPack.copy(mix = newMix) toJson
  }

  def stateOverview(): String =
    StateController.getOverview mkString ","

  def runningUnpackedTasks(): JsValue =
    StateController.runningUnpacked.toJson

  def runningTasksCount(): JsValue =
    StateController.runningUnpacked.groupBy(_.name).mapValues(_.size).toJson

  def killTask(taskId: String): JsValue  = {
    StateController.runningUnpacked.find(_.taskId.equalsIgnoreCase(taskId)).foreach(StateController.removeRunningUnpacked)
    val tasks = StateController.addToKill(TaskID.newBuilder().setValue(taskId).build())
    for(task <- tasks) {
      BaristaCalls.kill(task)
    }
    tasks.map(_.getValue) toJson
  }

  def teardown(): String  = {
    BaristaCalls.teardown()
    StateController.clean()
    "We are closed"
  }
}
