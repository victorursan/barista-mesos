package com.victorursan.docker


/**
  * Created by victor on 5/28/17.
  */
case class DockerStatus(taskId: String, dateTime: Long, cpuPer: Double, memPer: Double, memUsage: Double, memAvailable: Double)
