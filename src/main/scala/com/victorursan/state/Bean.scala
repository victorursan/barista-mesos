package com.victorursan.state

/**
  * Created by victor on 4/23/17.
  */

case class Bean(taskId: String, dockerEntity: DockerEntity) {
  def schedule(agentID: String, offerId: String) = ScheduledBean(taskId, dockerEntity, agentID, offerId)
}
case class ScheduledBean(taskId: String, dockerEntity: DockerEntity, agentId: String, offerId: String)
