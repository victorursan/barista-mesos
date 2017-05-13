package com.victorursan.consul

import java.net.URL

import com.victorursan.state.Bean

/**
  * Created by victor on 5/13/17.
  */
object Utils {

  def convertBeanToService(bean: Bean, servicePort: Int): BaristaService =
    BaristaService(bean.taskId, bean.name, serviceAddress = bean.hostname.get, servicePort = servicePort,
      checks = bean.checks.map(bc => BaristaCheck(new URL(s"http://${bean.hostname.get}:$servicePort${bc.httpPath}"), bc.interval)))

}
