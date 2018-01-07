package com.victorursan.state

/**
  * Created by victor on 3/12/17.
  */

case class DockerPort(containerPort: Int, hostPort: Option[Int])

case class DockerResource(cpu: Double, mem: Double, disk: Option[Double], ports: List[DockerPort] = List())

case class DockerEntity(image: String, role: String = "*", network: String = "bridge", resource: DockerResource, arguments: List[String] = List())
