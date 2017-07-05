package com.victorursan.barista

import com.victorursan.state.Bean
import com.victorursan.zookeeper.StateController

object BeanUtils {
  def resetBean(bean: Bean): Bean =
    bean.copy(id = StateController.getNextId, agentId = None, dockerEntity = bean.dockerEntity.copy(
      resource = bean.dockerEntity.resource.copy(ports = bean.dockerEntity.resource.ports.map(dockerPort => dockerPort.copy(hostPort = None)))
    ))
}
