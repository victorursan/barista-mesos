package com.victorursan.barista

import akka.actor.ActorSystem
import com.victorursan.barista.scheduler.HostCompressionScheduler
import com.victorursan.consul.ServiceController
import com.victorursan.state._
import com.victorursan.utils.{JsonSupport, MesosConf}
import com.victorursan.zookeeper.StateController
import org.apache.mesos.v1.Protos.{OfferID, TaskID}
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

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
    system.scheduler.schedule(1 seconds, schedulerTWindow seconds) {
      val beans = StateController.awaitingBeans
      val offers = StateController.availableOffers

      if (offers.nonEmpty && beans.nonEmpty) {

        val ScheduleState(scheduledBeans, canceledOffers, consumedBeans) =
          if (StateController.isDefragmenting) HostCompressionScheduler.schedule(beans, offers.toList) else schedulerAlgorithm.schedule(beans, offers.toList)

        StateController.addToRunningUnpacked(scheduledBeans.map(_._1))
        StateController.removeFromAccept(beans.filter(bean => consumedBeans.contains(bean.taskId)))
        scheduledBeans.foreach { case (bean: Bean, offerID: String) => BaristaCalls.acceptContainer(bean, offerID) }

        //        val newBeans = beans.map(bean => bean.copy(agentId = Some(offers.head.agentId)))
        //        BaristaCalls.acceptContainers(newBeans, offers.head.id)
        //
        //        BaristaCalls.decline(List(OfferID.newBuilder().setValue(offers.tail.head.id).build()))
        BaristaCalls.decline(canceledOffers.map(off => OfferID.newBuilder().setValue(off.id).build()))

        StateController.removeFromOffer(offers.map(_.id))
      } else {
        BaristaCalls.decline(offers.map(off => OfferID.newBuilder().setValue(off.id).build()))
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
      val tasks = similarBeans.take(scaleQuantity).map(_.taskId)
      killTask(tasks)
    } else if (scaleQuantity < 0) { //add some
      similarBeans.headOption.foreach(bean =>
        StateController.addToAccept(
          (1 to -scaleQuantity)
            .map(_ => BeanUtils.resetBean(bean))
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
    StateController.saveAutoScaling(pack.name, pack.autoScaling)
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
    system.scheduler.schedule(1 seconds, 2 seconds) {
      if (StateController.tasksToKill.isEmpty) { //todo this will throw an error after the first true
        BaristaCalls.teardown()
        StateController.clean()
      }

    }
    "We are closed"
  }

  def killTask(tasksIds: Set[String]): JsValue = {
    val toKill = StateController.runningUnpacked.filter { (bean: Bean) => tasksIds.contains(bean.taskId) }
    StateController.removeRunningUnpacked(toKill)
    toKill.foreach(bean =>
      drain(bean) match {
        case Success(bbean) =>
          ServiceController.deregisterService(bbean.hostname.get, bbean.taskId)
          StateController.removeFromBeanDocker(bbean.taskId)
        case _ => Unit
      }
    )
    // todo

    val tasks = StateController.addToKill(tasksIds)
    for (task <- tasks) {
      BaristaCalls.kill(TaskID.newBuilder().setValue(task).build())
    }
    tasks toJson
  }

  private def drain(bean: Bean): Try[Bean] = {

    Success(bean)
  }

  def defragment(): JsValue = {
    val runningBeans = StateController.runningUnpacked.toList
    Future {
      defragment(runningBeans)
    }
    "Started the defragmentation process" toJson
  }

  private def defragment(beans: List[Bean]): Unit = {
    StateController.setDefragmenting(true)
    val agentResources = StateController.agentResources
    var toScheduleBeans: List[Bean] = beans.sortBy(bean =>
      if (schedulerResource == "mem")
        (bean.dockerEntity.resource.mem, agentResources(bean.agentId.get).mem)
      else
        (bean.dockerEntity.resource.cpu, agentResources(bean.agentId.get).cpus)).reverse

    while (toScheduleBeans.nonEmpty) {
      val bean = toScheduleBeans.head
      toScheduleBeans = toScheduleBeans.filterNot(_.taskId == bean.taskId)
      val resetedBean = BeanUtils.resetBean(bean)
      StateController.addToAccept(resetedBean)
      waitRunning(resetedBean) match {
        case Success(newBean) =>
          toScheduleBeans = toScheduleBeans.filterNot(bbean => bbean.agentId == newBean.agentId)
          killTask(Set(bean.taskId))
          Thread.sleep(1000)
        case _ => None
      }
    }
    StateController.setDefragmenting(false)
  }

  private def waitRunning(bean: Bean): Try[Bean] = {
    for (_ <- 1 to drainTimeout) {
      Thread.sleep(1000)
      val running = StateController.runningUnpacked
      running.find(_.taskId == bean.taskId)
        .foreach(bbbbean => {
          return Success(bbbbean)
        })
    }
    Failure(new Throwable("a"))
  }

  def upgrade(upgrade: UpgradeBean): Set[Bean] = {
    StateController.runningUnpacked.filter(bean => bean.name == upgrade.name && bean.pack == upgrade.pack)
      .flatMap(oldBean => {
        val taskId = StateController.getNextId
        val newBean = upgrade.newBean.toBean(taskId)
        StateController.addToAccept(newBean)

        waitRunning(newBean) match {

          case Success(newBBean) =>
            killTask(Set(oldBean.taskId))
            Thread.sleep(1000)
            Some(newBBean)
          case _ => None
        }
      })
  }

}

object BaristaController {
  val loadBalancing = "leastconn"
}