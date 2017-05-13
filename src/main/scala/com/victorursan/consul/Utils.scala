package com.victorursan.consul

import com.victorursan.state.Bean

/**
  * Created by victor on 5/13/17.
  */
object Utils {

  def convertBeanToService(bean: Bean, servicePort: Int): BaristaService =
    BaristaService(bean.taskId, bean.name, serviceAddress = bean.hostname.get, servicePort = servicePort)

}
