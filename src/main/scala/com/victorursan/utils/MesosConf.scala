package com.victorursan.utils

import java.net.URI

import com.typesafe.config.ConfigFactory

trait MesosConf {
  private val config = ConfigFactory.load("barista.conf")
  private val mesosConfig = config.getConfig("mesos")
  private val userAEConfig = config.getConfig("userAgentEntries")

  val frameworkName: String = mesosConfig.getString("frameworkName")
  val frameworkId: String = mesosConfig.getString("frameworkId")
  val mesosUri: URI = URI.create(mesosConfig.getString("mesosUri"))
  val role: String = mesosConfig.getString("role")
  val user: String = mesosConfig.getString("user")
  val failoverTimeout: Int = mesosConfig.getInt("failoverTimeout")

  val userAEName: String = userAEConfig.getString("name")
  val userAEVersion: String = userAEConfig.getString("version")
}
