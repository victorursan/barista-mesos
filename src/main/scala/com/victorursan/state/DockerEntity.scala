package com.victorursan.state

/**
  * Created by victor on 3/12/17.
  */
case class DockerResource(cpu: Double, mem: Double)

case class DockerEntity(name: String, image: String, role: String = "*", resource: DockerResource)
