package com.victorursan.consul

import java.net.URL

/**
  * Created by victor on 5/11/17.
  */
case class BaristaService(id: String, name: String, tags: List[String] = List(), serviceAddress: String, servicePort: Int, checks: List[BaristaCheck] = List())

case class BaristaCheck(httpHealth: URL, interval: Int = 5)