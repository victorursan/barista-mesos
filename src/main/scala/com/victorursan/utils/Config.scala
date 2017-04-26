package com.victorursan.utils

import com.typesafe.config.ConfigFactory

/**
  * Created by victor on 4/2/17.
  */
trait Config {
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
//  private val httpConfig = config.getConfig("http")
  val httpInterface: String = httpConfig.getString("interface")
  val httpPort: Int = httpConfig.getInt("port")
}
