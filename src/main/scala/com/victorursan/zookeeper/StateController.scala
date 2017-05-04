package com.victorursan.zookeeper

import java.nio.charset.StandardCharsets

import com.victorursan.state.{Bean, DockerEntity}
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
  private val runningPath = s"$basePath/running"
  private val historyAwaitingPath = s"$basePath/historyAwaiting"
  private val nextIdPath = s"$basePath/nextId"
  private val killingPath = s"$basePath/killing"
  private val overviewPath = s"$basePath/overview"

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

  override def addToRunning(bean: Bean): Set[Bean] = addToRunning(Set(bean))

  override def addToRunning(beans: Set[Bean]): Set[Bean] = {
    val newRunning = running ++ beans
    CuratorService.createOrUpdate(runningPath, newRunning.toJson.toString().getBytes)
    newRunning
  }

  override def running: Set[Bean] =
    Try(new String(CuratorService.read(runningPath))
      .parseJson
      .convertTo[Set[Bean]])
      .getOrElse(Set())

  override def removeRunning(bean: Bean): Set[Bean] = removeRunning(Set(bean))

  override def removeRunning(beans: Set[Bean]): Set[Bean] = {
    val newRunning = running diff beans
    CuratorService.createOrUpdate(runningPath, newRunning.toJson.toString().getBytes)
    newRunning
  }

  override def addToAccept(bean: Bean): Set[Bean] = {
    val newBeans: Set[Bean] = awaitingBeans + bean
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
}
