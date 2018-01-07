package com.victorursan.utils

import com.typesafe.config.ConfigFactory

/**
  * Created by victor on 4/2/17.
  */
trait HttpConfig {
  val httpInterface: String = httpConfig.getString("interface")
  val httpPort: Int = httpConfig.getInt("port")
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
}
