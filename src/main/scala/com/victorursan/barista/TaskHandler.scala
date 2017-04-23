package com.victorursan.barista

import com.victorursan.state.DockerEntity
import org.apache.mesos.v1.Protos.ContainerInfo.DockerInfo
import org.apache.mesos.v1.Protos._

import scala.language.postfixOps

/**
  * Created by victor on 3/12/17.
  */
object TaskHandler {

  private val uniqueNumber = (1 to Int.MaxValue) iterator

  def createTaskWith(agentID: AgentID, dockerEntity: DockerEntity): TaskInfo = {
    val taskID: TaskID = createNexTaskID
    val dockerInfo: DockerInfo = createDockerInfo(dockerEntity.image)
    val containerInfo: ContainerInfo = createContainerInfo(dockerInfo)
    createDockerTask(taskID, agentID, containerInfo, dockerEntity)
  }

  private def createNexTaskID: TaskID =
    TaskID.newBuilder
      .setValue((uniqueNumber next) toString)
      .build

  private def createDockerInfo(image: String): DockerInfo =
    DockerInfo.newBuilder
      .setImage(image)
      .setForcePullImage(true)
      .setNetwork(ContainerInfo.DockerInfo.Network.HOST)
      .build

  private def createContainerInfo(dockerInfo: DockerInfo): ContainerInfo =
    ContainerInfo.newBuilder
      .setType(ContainerInfo.Type.DOCKER)
      .setDocker(dockerInfo)
      .build

  private def createDockerTask(taskID: TaskID, agentID: AgentID, containerInfo: ContainerInfo, dockerEntity: DockerEntity): TaskInfo =
    TaskInfo.newBuilder
      .setName(dockerEntity.name)
      .setTaskId(taskID)
      .setAgentId(agentID)
      .addResources(createScalarResource("cpus", dockerEntity.resource.cpu))
      .addResources(createScalarResource("mem", dockerEntity.resource.mem))
      .setContainer(containerInfo)
      .setCommand(CommandInfo.newBuilder.setShell(false))
      .build

  private def createScalarResource(name: String, value: Double): Resource =
    Resource.newBuilder
      .setName(name)
      .setType(Value.Type.SCALAR)
      .setScalar(Value.Scalar.newBuilder.setValue(value))
      .build
}
