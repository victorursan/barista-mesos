package com.victorursan.zookeeper

import com.victorursan.state.Bean
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

  def addToAccept(bean: Bean): Set[Bean]

  def removeFromAccept(bean: Bean): Set[Bean]

  def removeFromAccept(beans: Set[Bean]): Set[Bean]

  def addToRunning(bean: Bean): Set[Bean]

  def addToOldBeans(bean: Bean): Set[Bean]

  def addToOldBeans(beans: Set[Bean]): Set[Bean]

  def oldBeans: Set[Bean]

  def removeOldBean(bean: Bean): Set[Bean]

  def removeOldBean(beans: Set[Bean]): Set[Bean]

  def addToRunning(beans: Set[Bean]): Set[Bean]

  def running: Set[Bean]

  def removeRunning(bean: Bean): Set[Bean]

  def removeRunning(beans: Set[Bean]): Set[Bean]

  def addToKill(taskID: TaskID): Set[TaskID]

  def removeFromKill(taskID: TaskID): Set[TaskID]

  def removeFromKill(taskIDs: Set[TaskID]): Set[TaskID]
}
