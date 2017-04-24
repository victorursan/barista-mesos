package com.victorursan.zookeeper

import com.victorursan.state.Bean
import com.victorursan.utils.JsonSupport
import org.apache.mesos.v1.Protos.TaskID
import spray.json._

import scala.util.Try

/**
  * Created by victor on 4/24/17.
  */
object StateController extends State with JsonSupport {
  private val basePath: String = "/barista/state"
  private val awaitingPath: String = s"$basePath/awaiting"
  private val killingPath: String = s"$basePath/killing"

  override def addToAccept(bean: Bean): List[Bean] = {
    val newBeans = bean :: awaitingBeans
    CuratorService.createOrUpdate(awaitingPath, newBeans.toJson.toString().getBytes)
    newBeans
  }

  override def awaitingBeans: List[Bean] =
    Try(CuratorService.read(awaitingPath)
      .mkString
      .parseJson
      .convertTo[List[Bean]])
      .getOrElse(List())

  override def addToKill(taskID: TaskID): List[TaskID] = {
    val newTasksKill = taskID :: tasksToKill
    CuratorService.createOrUpdate(awaitingPath, newTasksKill.map(_.getValue).toJson.toString().getBytes)
    newTasksKill
  }

  override def tasksToKill: List[TaskID] =
    Try(CuratorService.read(killingPath)
      .mkString
      .parseJson
      .convertTo[List[String]]
      .map(TaskID.newBuilder()
        .setValue(_)
        .build()))
      .getOrElse(List())

}
