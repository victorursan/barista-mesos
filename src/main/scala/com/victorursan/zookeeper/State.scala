package com.victorursan.zookeeper

import com.victorursan.state.{Bean, DockerEntity}
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

  def addToKill(taskID: TaskID): Set[TaskID]

  def removeFromKill(taskID: TaskID): Set[TaskID]

  def removeFromKill(taskIDs: Set[TaskID]): Set[TaskID]
}
