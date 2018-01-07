package com.victorursan.zookeeper

import java.nio.charset.StandardCharsets

import com.victorursan.state._
import com.victorursan.utils.JsonSupport
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
  private val agentResourcesPath = s"$basePath/agentResources"
  private val schedulerPath = s"$basePath/scheduler"
  private val roundRobinPath = s"$schedulerPath/roundRobin"
  private val defragmentingPath = s"$schedulerPath/defragmenting"
  private val autoscalingPath = s"$basePath/autoscaling"


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

  override def removeOldBean(bean: Bean): Set[Bean] = removeOldBean(Set(bean))

  override def removeOldBean(beans: Set[Bean]): Set[Bean] = {
    val newOldBeans = oldBeans diff beans
    CuratorService.createOrUpdate(historyAwaitingPath, newOldBeans.toJson.toString().getBytes)
    newOldBeans
  }

  override def oldBeans: Set[Bean] =
    Try(new String(CuratorService.read(historyAwaitingPath))
      .parseJson
      .convertTo[Set[Bean]])
      .getOrElse(Set())

  override def addToRunningUnpacked(bean: Bean): Set[Bean] = addToRunningUnpacked(Set(bean))

  override def addToRunningUnpacked(beans: Set[Bean]): Set[Bean] = {
    val newRunning = runningUnpacked ++ beans
    CuratorService.createOrUpdate(runningUnpackedPath, newRunning.toJson.toString().getBytes)
    newRunning
  }

  override def removeRunningUnpacked(bean: Bean): Set[Bean] = removeRunningUnpacked(Set(bean))

  override def removeRunningUnpacked(beans: Set[Bean]): Set[Bean] = {
    val newRunning = runningUnpacked.filterNot(t => beans.map(_.taskId).contains(t.taskId))
    CuratorService.createOrUpdate(runningUnpackedPath, newRunning.toJson.toString().getBytes)
    newRunning
  }

  def removeRunningUnpacked(taskId: String): Set[Bean] = {
    val newRunning = runningUnpacked.filterNot(t => t.taskId.equalsIgnoreCase(taskId))
    CuratorService.createOrUpdate(runningUnpackedPath, newRunning.toJson.toString().getBytes)
    newRunning
  }

  override def runningUnpacked: Set[Bean] =
    Try(new String(CuratorService.read(runningUnpackedPath))
      .parseJson
      .convertTo[Set[Bean]])
      .getOrElse(Set())

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

  override def addToKill(taskID: String): Set[String] = addToKill(Set(taskID))

  override def addToKill(tasksID: Set[String]): Set[String] = {
    val newTasksKill = tasksToKill ++ tasksID
    CuratorService.createOrUpdate(killingPath, newTasksKill.toJson.toString().getBytes)
    newTasksKill
  }

  override def removeFromKill(taskID: String): Set[String] = removeFromKill(Set(taskID))

  override def removeFromKill(taskIDs: Set[String]): Set[String] = {
    val newTasksKill = tasksToKill diff taskIDs
    CuratorService.createOrUpdate(killingPath, newTasksKill.toJson.toString().getBytes)
    newTasksKill
  }

  override def tasksToKill: Set[String] =
    Try(new String(CuratorService.read(killingPath))
      .parseJson
      .convertTo[Set[String]])
      .getOrElse(Set())


  override def addToOffer(offer: Offer): Set[Offer] = addToOffer(Set(offer))

  override def addToOffer(offers: Set[Offer]): Set[Offer] = {
    val newOffers: Set[Offer] = availableOffers ++ offers
    CuratorService.createOrUpdate(offersPath, newOffers.toJson.toString().getBytes(StandardCharsets.UTF_8))
    newOffers
  }

  override def removeFromOffer(offerId: String): Set[Offer] = removeFromOffer(Set(offerId))

  override def removeFromOffer(offersId: Set[String]): Set[Offer] = {
    val newOffers = availableOffers.filterNot(offer => offersId.contains(offer.id))
    CuratorService.createOrUpdate(offersPath, newOffers.toJson.toString().getBytes)
    newOffers
  }

  override def availableOffers: Set[Offer] =
    Try(new String(CuratorService.read(offersPath))
      .parseJson
      .convertTo[Set[Offer]])
      .getOrElse(Set())

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

  override def agentResources: Map[String, AgentResources] =
    Try(new String(CuratorService.read(agentResourcesPath))
      .parseJson
      .convertTo[Map[String, AgentResources]])
      .getOrElse(Map())

  override def updateAgentResources(agentResources: Map[String, AgentResources]): Map[String, AgentResources] = {
    CuratorService.createOrUpdate(agentResourcesPath, agentResources.toJson.toString().getBytes(StandardCharsets.UTF_8))
    agentResources
  }

  def updateRoundRobinIndex(index: Int): Int = {
    CuratorService.createOrUpdate(roundRobinPath, index.toJson.toString().getBytes(StandardCharsets.UTF_8))
    index
  }

  def incRoundRobinIndex: Int = {
    val incrementedValue = roundRobinIndex + 1
    CuratorService.createOrUpdate(roundRobinPath, incrementedValue.toJson.toString().getBytes(StandardCharsets.UTF_8))
    incrementedValue
  }

  def roundRobinIndex: Int =
    Try(new String(CuratorService.read(roundRobinPath))
      .parseJson
      .convertTo[Int])
      .getOrElse(0)

  def setDefragmenting(defragmenting: Boolean): Boolean = {
    CuratorService.createOrUpdate(defragmentingPath, defragmenting.toJson.toString().getBytes(StandardCharsets.UTF_8))
    defragmenting
  }

  def isDefragmenting: Boolean =
    Try(new String(CuratorService.read(defragmentingPath))
      .parseJson
      .convertTo[Boolean])
      .getOrElse(false)

  def getAutoScaling(pack: String): AutoScaling =
    Try(new String(CuratorService.read(s"$autoscalingPath/$pack"))
      .parseJson
      .convertTo[AutoScaling])
      .getOrElse(AutoScaling(algorithm = "static-threashold", resource = "mem", thresholds = Thresholds(List(20, 60), List(10, 10), List(30, 30), List(1, 9))))

  def saveAutoScaling(pack: String, autoScaling: AutoScaling): AutoScaling = {
    CuratorService.createOrUpdate(s"$autoscalingPath/$pack", autoScaling.toJson.toString().getBytes(StandardCharsets.UTF_8))
    autoScaling
  }

  def clean(): Unit = CuratorService.delete(basePath)

  def cleanOffers(): Unit = CuratorService.delete(offersPath)
}
