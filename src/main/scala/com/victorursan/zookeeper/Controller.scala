package com.victorursan.zookeeper

import com.victorursan.state.Bean
import org.apache.mesos.v1.Protos.TaskID

/**
  * Created by victor on 4/24/17.
  */
object Controller extends State {
  private val path: String = "/barista/state/"

  override def awaitingBeans: List[Bean] = ???

  override def tasksToKill: List[TaskID] = ???

  def addToAccept(bean: Bean) = ???

  def addToKill(taskID: TaskID) = ???

}
