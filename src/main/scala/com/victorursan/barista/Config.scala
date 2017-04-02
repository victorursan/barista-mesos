package com.victorursan.barista

import com.typesafe.config.ConfigFactory

/**
  * Created by victor on 4/2/17.
  */
trait Config {
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  val httpInterface = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")
}
