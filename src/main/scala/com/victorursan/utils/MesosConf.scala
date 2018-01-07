package com.victorursan.utils

import java.net.URI

import com.typesafe.config.ConfigFactory
import com.victorursan.barista.scheduler.{EvenOutScheduler, HostCompressionScheduler, RoundRobinScheduler, Scheduler}


trait MesosConf {
  val schedulerAlgorithms: Map[String, Scheduler] =
    Map(
      "round-robin" -> RoundRobinScheduler,
      "host-compression" -> HostCompressionScheduler,
      "even-out" -> EvenOutScheduler)
  val frameworkName: String = mesosConfig.getString("frameworkName")
  val frameworkId: String = mesosConfig.getString("frameworkId")
  val mesosUri: URI = URI.create(mesosConfig.getString("mesosUri"))
  val role: String = mesosConfig.getString("role")
  val user: String = mesosConfig.getString("user")
  val failoverTimeout: Int = mesosConfig.getInt("failoverTimeout")
  val userAEName: String = userAEConfig.getString("name")
  val userAEVersion: String = userAEConfig.getString("version")
  val schedulerTWindow: Int = schedulerConfig.getInt("timeWindow")
  val schedulerResource: String = schedulerConfig.getString("resource")
  val schedulerAlgorithm: Scheduler = schedulerAlgorithms(schedulerAlgorithmStr)
  val drainTimeout: Int = otherConfig.getInt("drainTimeout")
  private val config = ConfigFactory.load("barista.conf")
  private val mesosConfig = config.getConfig("mesos")
  private val userAEConfig = config.getConfig("userAgentEntries")
  private val schedulerConfig = config.getConfig("scheduler")
  private val otherConfig = config.getConfig("barista")
  private val schedulerAlgorithmStr: String = schedulerConfig.getString("algorithm")
}

