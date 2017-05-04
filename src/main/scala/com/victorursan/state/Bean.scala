package com.victorursan.state

/**
  * Created by victor on 4/23/17.
  */

case class Bean(taskId: String, dockerEntity: DockerEntity, pack: Option[String] = None, agentId: Option[String] = None, offerId: Option[String] = None)
