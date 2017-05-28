package com.victorursan.barista

import java.net.URI
import java.util.UUID

import akka.actor.ActorSystem
import com.mesosphere.mesos.rx.java.util.UserAgentEntries
import com.victorursan.state._
import com.victorursan.utils.JsonSupport
import com.victorursan.zookeeper.StateController
import org.apache.mesos.v1.Protos.{OfferID, TaskID}
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by victor on 4/2/17.
  */
class BaristaController extends JsonSupport {
  private val fwName = "Barista"
  private val fwId = s"$fwName-${UUID.randomUUID}"
  private val mesosUri = URI.create("http://10.1.1.11:5050/api/v1/scheduler")
  private val role = "*"
  private implicit val system: ActorSystem = ActorSystem("Barista-controller-actor-system")
  private implicit val ec: ExecutionContext = system.dispatcher


  def start(): Unit = {
    if (StateController.availableOffers.nonEmpty) {
    StateController.cleanOffers()
    }
    system.scheduler.schedule(1 seconds, 4 seconds) {
      val beans = StateController.awaitingBeans
      val offers = StateController.availableOffers

      if (offers.nonEmpty && beans.nonEmpty) {
        val ScheduleState(scheduledBeans, canceledOffers, consumedBeans) = BaristaScheduler.scheduleBeans(beans, offers.toList)

        StateController.addToRunningUnpacked(scheduledBeans.map(_._1))
        StateController.removeFromAccept(consumedBeans)
        scheduledBeans.foreach { case (bean: Bean, offerID: String) => BaristaCalls.acceptContainer(bean, offerID) }

//        val newBeans = beans.map(bean => bean.copy(agentId = Some(offers.head.agentId)))
//        BaristaCalls.acceptContainers(newBeans, offers.head.id)
//
//        BaristaCalls.decline(List(OfferID.newBuilder().setValue(offers.tail.head.id).build()))
        BaristaCalls.decline(canceledOffers.map(off => OfferID.newBuilder().setValue(off.id).build()))

        StateController.removeFromOffer(offers.map(_.id))
      }
    }
    BaristaCalls.subscribe(mesosUri, fwName, 10, role, UserAgentEntries.literal("com.victorursan", "barista"), fwId)
  }

  def launchRawBean(rawBean: RawBean): JsValue = {
    val taskId = StateController.getNextId
    val beans = StateController.addToAccept(rawBean.toBean(taskId))
    beans.toJson
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

  def killTask(taskId: String): JsValue = {
    StateController.runningUnpacked.find(_.taskId.equalsIgnoreCase(taskId)).foreach(StateController.removeRunningUnpacked)
    val tasks = StateController.addToKill(TaskID.newBuilder().setValue(taskId).build())
    for (task <- tasks) {
      BaristaCalls.kill(task)
    }
    tasks.map(_.getValue) toJson
  }

  def availableOffers: JsValue = StateController.availableOffers toJson

  def teardown(): String = {
    BaristaCalls.teardown()
    StateController.clean()
    "We are closed"
  }
}
