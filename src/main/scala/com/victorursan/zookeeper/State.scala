package com.victorursan.zookeeper

import com.victorursan.state.Bean
import org.apache.mesos.v1.Protos.TaskID

/**
  * Created by victor on 4/24/17.
  */
trait State {
  def awaitingBeans: List[Bean]

  def tasksToKill: List[TaskID]

  def addToAccept(bean: Bean)

  def addToKill(taskID: TaskID)
}
