package com.victorursan.barista

import com.victorursan.state.{Bean, DockerEntity, DockerPort}
import org.apache.mesos.v1.Protos.ContainerInfo.DockerInfo
import org.apache.mesos.v1.Protos.ContainerInfo.DockerInfo.{Network, PortMapping}
import org.apache.mesos.v1.Protos._

import scala.collection.JavaConverters._
import scala.language.postfixOps

/**
  * Created by victor on 3/12/17.
  */
object TaskHandler {

  def createTaskWith(agentID: AgentID, bean: Bean): TaskInfo = {
    val taskID: TaskID = createTaskId(bean.taskId)
    val dockerInfo: DockerInfo = createDockerInfo(bean.dockerEntity)
    val containerInfo: ContainerInfo = createContainerInfo(dockerInfo)
    createDockerTask(taskID, agentID, containerInfo, bean.dockerEntity, bean.name)
  }

  private def createTaskId(taskId: String): TaskID =
    TaskID.newBuilder
      .setValue(taskId)
      .build

  private def createDockerInfo(dockerEntity: DockerEntity): DockerInfo =
    DockerInfo.newBuilder
      .setImage(dockerEntity.image)
      .setForcePullImage(true)
      .addAllPortMappings(dockerEntity.resource.ports.map(dockerPortToPortMapping).asJava)
      .setNetwork(dockerNetwork(dockerEntity.network))
      .build

  private def dockerNetwork(network: String): Network = network match {
    case "bridge" => Network.BRIDGE
    case "host" => Network.HOST
    case "user" => Network.USER
    case "none" => Network.NONE
  }

  private def dockerPortToPortMapping(dockerPort: DockerPort): PortMapping =
    if (dockerPort.hostPort.isDefined) {
      PortMapping.newBuilder().setContainerPort(dockerPort.containerPort).setHostPort(dockerPort.hostPort.get).build()
    } else {
      PortMapping.newBuilder().setContainerPort(dockerPort.containerPort).setHostPort(dockerPort.containerPort).build() // todo throw error, should no be possible
    }

  private def createContainerInfo(dockerInfo: DockerInfo): ContainerInfo =
    ContainerInfo.newBuilder
      .setType(ContainerInfo.Type.DOCKER)
      .setDocker(dockerInfo)
      .build

  private def createDockerTask(taskID: TaskID, agentID: AgentID, containerInfo: ContainerInfo, dockerEntity: DockerEntity, taskName: String): TaskInfo =
    TaskInfo.newBuilder
      .setName(taskName)
      .setTaskId(taskID)
      .setAgentId(agentID)
      .addResources(createScalarResource("cpus", dockerEntity.resource.cpu, dockerEntity.role))
      .addResources(createScalarResource("mem", dockerEntity.resource.mem, dockerEntity.role))
      .addResources(createPortsRangeResource("ports", dockerEntity.resource.ports, dockerEntity.role))
      .setContainer(containerInfo)
      .setCommand(CommandInfo.newBuilder.setShell(false).addAllArguments(dockerEntity.arguments.asJava))
      .build

  private def createScalarResource(name: String, value: Double, role: String): Resource =
    Resource.newBuilder
      .setName(name)
      .setRole(role)
      .setType(Value.Type.SCALAR)
      .setScalar(Value.Scalar.newBuilder.setValue(value))
      .build

  private def createPortsRangeResource(name: String, ports: List[DockerPort], role: String): Resource =
    Resource.newBuilder
      .setName(name)
      .setRole(role)
      .setType(Value.Type.RANGES)
      .setRanges(Value.Ranges.newBuilder()
        .addAllRange(ports.map(dockerPortToValueRange).asJava)
        .build())
      .build

  private def dockerPortToValueRange(dockerPort: DockerPort): Value.Range =
    if (dockerPort.hostPort.isDefined) {
      Value.Range.newBuilder().setBegin(dockerPort.hostPort.get).setEnd(dockerPort.hostPort.get).build()
    } else {
      Value.Range.newBuilder().setBegin(dockerPort.containerPort).setEnd(dockerPort.containerPort).build() // todo throw error, should no be possible
    }


}
