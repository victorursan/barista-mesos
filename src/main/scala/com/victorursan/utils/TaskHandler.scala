package com.victorursan.utils

import org.apache.mesos.Protos
import org.apache.mesos.Protos.ContainerInfo.DockerInfo
import org.apache.mesos.Protos._

import scala.language.postfixOps

/**
 * Created by victor on 4/24/16.
 */
object TaskHandler {

  private val uniqueNumber = (1 to Int.MaxValue) iterator

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

  private def createScalarResource(name: String, value: Double): Protos.Resource =
    Resource.newBuilder
      .setName(name)
      .setType(Value.Type.SCALAR)
      .setScalar(Value.Scalar.newBuilder.setValue(value))
      .build

  private def createDockerTask(taskID: TaskID, offer: Offer, containerInfo: ContainerInfo, dockerEntity: DockerEntity): TaskInfo =
    TaskInfo.newBuilder
      .setName(dockerEntity.name)
      .setTaskId(taskID)
      .setSlaveId(offer.getSlaveId)
      .addResources(createScalarResource("cpus", dockerEntity.resource.cpu))
      .addResources(createScalarResource("mem", dockerEntity.resource.mem))
      .setContainer(containerInfo)
      .setCommand(CommandInfo.newBuilder.setShell(false))
      .build

  def createTaskWith(offer: Offer, dockerEntity: DockerEntity): TaskInfo = {
    val taskID: TaskID = createNexTaskID
    val dockerInfo: DockerInfo = createDockerInfo(dockerEntity.image)
    val containerInfo: ContainerInfo = createContainerInfo(dockerInfo)
    createDockerTask(taskID, offer, containerInfo, dockerEntity)
  }
}
