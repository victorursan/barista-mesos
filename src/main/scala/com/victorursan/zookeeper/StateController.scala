package com.victorursan.zookeeper

import java.nio.charset.StandardCharsets

import com.victorursan.state.{Bean, BeanDocker, Offer, Pack}
import com.victorursan.utils.JsonSupport
import org.apache.mesos.v1.Protos.TaskID
import spray.json._
import scala.util.Try

/**
  * Created by victor on 4/24/17.
  */
object StateController extends JsonSupport with State {


  private val basePath = "/barista/state"
  private val awaitingPath = s"$basePath/awaiting"
  private val offersPath = s"$basePath/offers"
  private val runningUnpackedPath = s"$basePath/running/unpacked"
  private val runningPackedPath = s"$basePath/running/packed"
  private val historyAwaitingPath = s"$basePath/historyAwaiting"
  private val nextIdPath = s"$basePath/nextId"
  private val killingPath = s"$basePath/killing"
  private val overviewPath = s"$basePath/overview"
  private val beanDockerPath = s"$basePath/beanDocker"


  override def addToOverview(taskId: String, state: String): Map[String, String] = {
    val newOverview = getOverview + (taskId -> state)
    CuratorService.createOrUpdate(overviewPath, newOverview.toJson.toString().getBytes)
    newOverview
  }

  override def getOverview: Map[String, String] = {
    Try(new String(CuratorService.read(overviewPath))
      .parseJson
      .convertTo[Map[String, String]])
      .getOrElse(Map())
  }

  override def removeFromOverview(taskId: String): Map[String, String] = {
    val newOverview = getOverview - taskId
    CuratorService.createOrUpdate(overviewPath, overviewPath.toJson.toString().getBytes)
    newOverview
  }

  override def addToOldBeans(bean: Bean): Set[Bean] = addToOldBeans(Set(bean))

  override def addToOldBeans(beans: Set[Bean]): Set[Bean] = {
    val newOldBeans = oldBeans ++ beans
    CuratorService.createOrUpdate(historyAwaitingPath, newOldBeans.toJson.toString().getBytes)
    newOldBeans
  }

  override def oldBeans: Set[Bean] =
    Try(new String(CuratorService.read(historyAwaitingPath))
      .parseJson
      .convertTo[Set[Bean]])
      .getOrElse(Set())

  override def removeOldBean(bean: Bean): Set[Bean] = removeOldBean(Set(bean))

  override def removeOldBean(beans: Set[Bean]): Set[Bean] = {
    val newOldBeans = oldBeans diff beans
    CuratorService.createOrUpdate(historyAwaitingPath, newOldBeans.toJson.toString().getBytes)
    newOldBeans
  }

  override def addToRunningUnpacked(bean: Bean): Set[Bean] = addToRunningUnpacked(Set(bean))

  override def addToRunningUnpacked(beans: Set[Bean]): Set[Bean] = {
    val newRunning = runningUnpacked ++ beans
    CuratorService.createOrUpdate(runningUnpackedPath, newRunning.toJson.toString().getBytes)
    newRunning
  }

  override def runningUnpacked: Set[Bean] =
    Try(new String(CuratorService.read(runningUnpackedPath))
      .parseJson
      .convertTo[Set[Bean]])
      .getOrElse(Set())

  override def removeRunningUnpacked(bean: Bean): Set[Bean] = removeRunningUnpacked(Set(bean))

  override def removeRunningUnpacked(beans: Set[Bean]): Set[Bean] = {
    val newRunning = runningUnpacked diff beans
    CuratorService.createOrUpdate(runningUnpackedPath, newRunning.toJson.toString().getBytes)
    newRunning
  }

  override def addToAccept(bean: Bean): Set[Bean] = addToAccept(Set(bean))

  override def addToAccept(beans: Set[Bean]): Set[Bean] = {
    val newBeans: Set[Bean] = awaitingBeans ++ beans
    CuratorService.createOrUpdate(awaitingPath, newBeans.toJson.toString().getBytes(StandardCharsets.UTF_8))
    newBeans
  }

  override def getNextId: String = {
    val nextId = Try(BigInt(CuratorService.read(nextIdPath)).toLong).getOrElse(0l) + 1l
    CuratorService.createOrUpdate(nextIdPath, BigInt(nextId).toByteArray)
    nextId.toString
  }

  override def removeFromAccept(bean: Bean): Set[Bean] = removeFromAccept(Set(bean))

  override def removeFromAccept(beans: Set[Bean]): Set[Bean] = {
    val newBeans = awaitingBeans diff beans
    CuratorService.createOrUpdate(awaitingPath, newBeans.toJson.toString().getBytes)
    newBeans
  }

  override def awaitingBeans: Set[Bean] =
    Try(new String(CuratorService.read(awaitingPath))
      .parseJson
      .convertTo[Set[Bean]])
      .getOrElse(Set())

  override def addToKill(taskID: TaskID): Set[TaskID] = {
    val newTasksKill = tasksToKill + taskID
    CuratorService.createOrUpdate(killingPath, newTasksKill.map(_.getValue).toJson.toString().getBytes)
    newTasksKill
  }

  override def removeFromKill(taskID: TaskID): Set[TaskID] = removeFromKill(Set(taskID))

  override def removeFromKill(taskIDs: Set[TaskID]): Set[TaskID] = {
    val newTasksKill = tasksToKill diff taskIDs
    CuratorService.createOrUpdate(killingPath, newTasksKill.map(_.getValue).toJson.toString().getBytes)
    newTasksKill
  }

  override def tasksToKill: Set[TaskID] =
    Try(new String(CuratorService.read(killingPath))
      .parseJson
      .convertTo[Set[String]]
      .map(TaskID.newBuilder()
        .setValue(_)
        .build()))
      .getOrElse(Set())


  override def addToOffer(offer: Offer): Set[Offer] = addToOffer(Set(offer))

  override def addToOffer(offers: Set[Offer]): Set[Offer] = {
    val newOffers: Set[Offer] = availableOffers ++ offers
    CuratorService.createOrUpdate(offersPath, newOffers.toJson.toString().getBytes(StandardCharsets.UTF_8))
    newOffers
  }

  override def availableOffers: Set[Offer] =
    Try(new String(CuratorService.read(offersPath))
      .parseJson
      .convertTo[Set[Offer]])
      .getOrElse(Set())

  override def removeFromOffer(offerId: String): Set[Offer] = removeFromOffer(Set(offerId))

  override def removeFromOffer(offersId: Set[String]): Set[Offer] = {
    val newOffers = availableOffers.filterNot(offer => offersId.contains(offer.id))
    CuratorService.createOrUpdate(offersPath, newOffers.toJson.toString().getBytes)
    newOffers
  }


  override def addToBeanDocker(beanDocker: BeanDocker): Set[BeanDocker] = addToBeanDocker(Set(beanDocker))

  override def addToBeanDocker(beanDockers: Set[BeanDocker]): Set[BeanDocker] = {
    val newBeanDocker: Set[BeanDocker] = availableBeanDocker ++ beanDockers
    CuratorService.createOrUpdate(beanDockerPath, newBeanDocker.toJson.toString().getBytes(StandardCharsets.UTF_8))
    newBeanDocker
  }

  override def removeFromBeanDocker(taskId: String): Set[BeanDocker] = removeFromBeanDocker(Set(taskId))

  override def removeFromBeanDocker(tasksId: Set[String]): Set[BeanDocker] = {
    val newBeanDocker = availableBeanDocker.filterNot(beanDocker => tasksId.contains(beanDocker.taskId))
    CuratorService.createOrUpdate(beanDockerPath, newBeanDocker.toJson.toString().getBytes)
    newBeanDocker
  }

  override def availableBeanDocker: Set[BeanDocker] =
    Try(new String(CuratorService.read(beanDockerPath))
      .parseJson
      .convertTo[Set[BeanDocker]])
      .getOrElse(Set())

  def clean(): Unit = CuratorService.delete(basePath)

  def cleanOffers(): Unit = CuratorService.delete(offersPath)
}
