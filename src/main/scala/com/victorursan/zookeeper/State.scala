package com.victorursan.zookeeper

import com.victorursan.state.{Bean, DockerEntity, ScheduledBean}
import org.apache.mesos.v1.Protos.TaskID

/**
  * Created by victor on 4/24/17.
  */
trait State {
  def getNextId: String

  def getOverview: Map[String, String]

  def addToOverview(taskId: String, state: String): Map[String, String]

  def removeFromOverview(taskId: String): Map[String, String]

  def awaitingBeans: Set[Bean]

  def tasksToKill: Set[TaskID]

  def addToAccept(dockerEntity: DockerEntity): Set[Bean]

  def removeFromAccept(bean: Bean): Set[Bean]

  def removeFromAccept(beans: Set[Bean]): Set[Bean]

  def addToRunning(bean: ScheduledBean): Set[ScheduledBean]

  def addToOldBeans(bean: Bean): Set[Bean]

  def addToOldBeans(beans: Set[Bean]): Set[Bean]

  def oldBeans: Set[Bean]

  def removeOldBean(bean: Bean): Set[Bean]

  def removeOldBean(beans: Set[Bean]): Set[Bean]

  def addToRunning(beans: Set[ScheduledBean]): Set[ScheduledBean]

  def running: Set[ScheduledBean]

  def removeRunning(bean: ScheduledBean): Set[ScheduledBean]

  def removeRunning(beans: Set[ScheduledBean]): Set[ScheduledBean]

  def addToKill(taskID: TaskID): Set[TaskID]

  def removeFromKill(taskID: TaskID): Set[TaskID]

  def removeFromKill(taskIDs: Set[TaskID]): Set[TaskID]
}
