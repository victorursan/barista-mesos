package com.victorursan.barista

import akka.actor.ActorSystem
import com.victorursan.state._
import com.victorursan.utils.{JsonSupport, MesosConf}
import com.victorursan.zookeeper.StateController
import org.apache.mesos.v1.Protos.{OfferID, TaskID}
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by victor on 4/2/17.
  */
class BaristaController extends JsonSupport with MesosConf {
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
    BaristaCalls.subscribe()
  }

  def launchRawBean(rawBean: RawBean): JsValue = {
    val taskId = StateController.getNextId
    val beans = StateController.addToAccept(rawBean.toBean(taskId))
    beans.toJson
  }

  def scaleBean(scaleBean: ScaleBean): JsValue = {
    val similarBeans = StateController.runningUnpacked.filter(bean => bean.pack.equals(scaleBean.pack) && bean.name.equalsIgnoreCase(scaleBean.name))
    //todo what happens if there is no similar beans? ( as in count 0 )
    val scaleQuantity = similarBeans.size - scaleBean.amount
    if (scaleQuantity > 0) { //kill some
      val tasks = StateController.addToKill(similarBeans.take(scaleQuantity).map(_.taskId))
      killTask(tasks)
    } else if (scaleQuantity < 0) { //add some
      similarBeans.headOption.foreach(bean =>
        StateController.addToAccept(
          (1 to -scaleQuantity)
            .map(_ => {
              bean.copy(id = StateController.getNextId, agentId = None, dockerEntity = bean.dockerEntity.copy(
                resource = bean.dockerEntity.resource.copy(ports = bean.dockerEntity.resource.ports.map(dockerPort => dockerPort.copy(hostPort = None)))
              ))

            })
            .toSet)
      )
    }
    scaleBean.toJson
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
    StateController.runningUnpacked.groupBy(_.pack)
      .map {
        case (Some(pack), beans) => Map(pack -> beans.groupBy(_.name).mapValues(_.size).toJson)
        case (None, beans) => beans.groupBy(_.name).mapValues(_.size.toJson)
      }.reduce(_ ++ _)
      .toJson

  def availableOffers: JsValue = StateController.availableOffers toJson

  def teardown(): String = {
    killTask(StateController.runningUnpacked.map(_.taskId))
    system.scheduler.schedule(1 seconds, 4 seconds) {
      if (StateController.tasksToKill.isEmpty) { //todo this will throw an error after the first true
        BaristaCalls.teardown()
        StateController.clean()
      }
    }
    "We are closed"
  }

  def killTask(tasksId: Set[String]): JsValue = {
    //    StateController.removeRunningUnpacked(StateController.runningUnpacked.filter{(bean: Bean) => tasksId.contains(bean.taskId)})
    val tasks = StateController.addToKill(tasksId)
    for (task <- tasks) {
      BaristaCalls.kill(TaskID.newBuilder().setValue(task).build())
    }
    tasks toJson
  }
}
