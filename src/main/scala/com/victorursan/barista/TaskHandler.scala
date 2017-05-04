package com.victorursan.barista

import com.victorursan.state.{Bean, DockerEntity}
import org.apache.mesos.v1.Protos.ContainerInfo.DockerInfo
import org.apache.mesos.v1.Protos._

import scala.language.postfixOps

/**
  * Created by victor on 3/12/17.
  */
object TaskHandler {

  def createTaskWith(agentID: AgentID, bean: Bean): TaskInfo = {
    val taskID: TaskID = createTaskId(bean.taskId)
    val dockerInfo: DockerInfo = createDockerInfo(bean.dockerEntity.image)
    val containerInfo: ContainerInfo = createContainerInfo(dockerInfo)
    createDockerTask(taskID, agentID, containerInfo, bean.dockerEntity, bean.name)
  }

  private def createTaskId(taskId: String): TaskID =
    TaskID.newBuilder
      .setValue(taskId)
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

  private def createDockerTask(taskID: TaskID, agentID: AgentID, containerInfo: ContainerInfo, dockerEntity: DockerEntity, taskName: String): TaskInfo =
    TaskInfo.newBuilder
      .setName(taskName)
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
