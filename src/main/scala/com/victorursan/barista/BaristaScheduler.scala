package com.victorursan.barista

import java.util

import com.victorursan.utils.DockerEntity
import org.apache.mesos.Protos.ContainerInfo.DockerInfo
import org.apache.mesos.Protos._
import org.apache.mesos.{ Scheduler, SchedulerDriver }
import org.slf4j.LoggerFactory

import scala.util.Random
import scala.collection.JavaConverters._
import scala.concurrent.Promise

class BaristaScheduler extends Scheduler {
  private[this] val log = LoggerFactory.getLogger(getClass)
  private[this] val promise: Promise[List[Offer]] = Promise[List[Offer]]
  val future = promise.future

  private var uncompletedTasks: List[DockerEntity] = List()

  def addTask(dockerEntity: DockerEntity): Unit = uncompletedTasks = dockerEntity :: uncompletedTasks

  def error(driver: SchedulerDriver, message: String): Unit = {
    println(s"error $message")
  }

  def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int): Unit = {
    println(s"executorLost")
  }

  def slaveLost(driver: SchedulerDriver, slaveId: SlaveID): Unit = {
    println(s"slaveLost")
  }

  def disconnected(driver: SchedulerDriver): Unit = {
    println(s"disconnected")
  }

  def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]): Unit = {
    println(s"frameworkMessage")
  }

  def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    println(s"statusUpdate \n----------\n $status \n-------------\n")
  }
  def offerRescinded(driver: SchedulerDriver, offerId: OfferID): Unit = {
    println(s"offerRescinded")
  }

  def resourceOffers(driver: SchedulerDriver, offers: util.List[Offer]): Unit = {
    println(s"resourceOffers $offers")
    promise.trySuccess(offers.asScala.toList)
    println(uncompletedTasks)
    uncompletedTasks match {
      case entity :: _ =>
        for (offer: Offer <- offers.asScala.toList) {
          // generate a unique task ID
          val taskId: TaskID = TaskID.newBuilder()
            .setValue(Math.abs(Random.nextInt).toString)
            .build()

          val portMappingBuilder: DockerInfo.PortMapping.Builder = DockerInfo.PortMapping.newBuilder()
            .setHostPort(entity.resource.port)
            .setContainerPort(entity.resource.port)
            .setProtocol("tcp")

          //docker info
          val dockerInfoBuilder: ContainerInfo.DockerInfo.Builder = ContainerInfo.DockerInfo.newBuilder()
            .setImage(entity.image)
            .setNetwork(ContainerInfo.DockerInfo.Network.HOST)
          // container info
          val containerInfoBuilder: ContainerInfo.Builder = ContainerInfo.newBuilder()
            .setType(ContainerInfo.Type.DOCKER)
            .setDocker(dockerInfoBuilder.build())

          val task: TaskInfo = TaskInfo.newBuilder()
            .setName(entity.name)
            .setTaskId(taskId)
            .setSlaveId(offer.getSlaveId)
            .addResources(Resource.newBuilder()
              .setName("cpus")
              .setType(Value.Type.SCALAR)
              .setScalar(Value.Scalar.newBuilder().setValue(entity.resource.cpu)))
            .addResources(Resource.newBuilder()
              .setName("mem")
              .setType(Value.Type.SCALAR)
              .setScalar(Value.Scalar.newBuilder().setValue(entity.resource.mem)))
            .setContainer(containerInfoBuilder.build())
            .setCommand(CommandInfo.newBuilder().setShell(false))
            .build()
          val filters: Filters = Filters.newBuilder().setRefuseSeconds(1).build()
          driver.launchTasks(List(offer.getId).asJavaCollection, List(task).asJavaCollection, filters)
        }
        uncompletedTasks = uncompletedTasks.tail
      case _ => println("nothing")
    }

  }

  def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo): Unit = {
    println(s"reregistered")
  }

  def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo): Unit = {
    println(s"registered")
  }
}
