package com.victorursan.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.victorursan.state._
import spray.json._

/**
  * Created by victor on 4/2/17.
  */
trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val beanCheckProtocol: RootJsonFormat[BeanCheck] = jsonFormat2(BeanCheck)
  implicit val beanDockerProtocol: RootJsonFormat[BeanDocker] = jsonFormat3(BeanDocker)
  implicit val scaleBeanProtocol: RootJsonFormat[ScaleBean] = jsonFormat3(ScaleBean)
  implicit val thresholdsProtocol: RootJsonFormat[Thresholds] = jsonFormat4(Thresholds)
  implicit val autoScalingProtocol: RootJsonFormat[AutoScaling] = jsonFormat3(AutoScaling)
  implicit val agentResourcesProtocol: RootJsonFormat[AgentResources] = jsonFormat3(AgentResources)

  implicit val upgradeBeanProtocol: RootJsonFormat[UpgradeBean] = new RootJsonFormat[UpgradeBean] {
    private val PACK = "pack"
    private val NAME = "name"
    private val NEW_BEAN = "newBean"

    override def write(upgradeBean: UpgradeBean): JsValue = {
      JsObject(List(
        upgradeBean.pack.map(pack => PACK -> pack.toJson),
        Some(NAME -> upgradeBean.name.toJson),
        Some(NEW_BEAN -> upgradeBean.newBean.toJson)
      ).flatten: _*)
    }

    override def read(json: JsValue): UpgradeBean = {
     val jsObject = json.asJsObject
      val packOpt = jsObject.fields.get(PACK).map(_.convertTo[String])
        jsObject.getFields(NAME, NEW_BEAN) match {
        case Seq(nameJs, newBeanJs) =>
          val name = nameJs.convertTo[String]
          val newBean = newBeanJs.convertTo[RawBean]
          UpgradeBean(packOpt, name, newBean)
        case other => deserializationError(s"Cannot deserialize UpgradeBean: invalid input. Raw input: $other")
      }
    }
  }

  implicit val resourceProtocol: RootJsonFormat[DockerResource] = new RootJsonFormat[DockerResource] {
    private val CPU = "cpu"
    private val MEMORY = "mem"
    private val DISK = "disk"
    private val PORTS = "ports"

    override def write(dockerResource: DockerResource): JsValue = {
      JsObject(List(
        Some(CPU -> dockerResource.cpu.toJson),
        Some(MEMORY -> dockerResource.mem.toJson),
        dockerResource.disk.map(disk => DISK -> disk.toJson),
        Some(PORTS -> dockerResource.ports.map(writeDockerPort).toJson)
      ).flatten: _*)
    }

    override def read(json: JsValue): DockerResource = {
      val jsObject = json.asJsObject
      val diskOpt = jsObject.fields.get(DISK).map(_.convertTo[Double])
      val ports = jsObject.fields.get(PORTS).map(_.convertTo[List[String]].map(readDockerPort)).getOrElse(List())
      jsObject.getFields(CPU, MEMORY) match {
        case Seq(cpuJs, memJs) =>
          val cpu = cpuJs.convertTo[Double]
          val mem = memJs.convertTo[Double]
          DockerResource(cpu = cpu, mem = mem, diskOpt, ports = ports)
        case other => deserializationError(s"Cannot deserialize DockerEntity: invalid input. Raw input: $other")
      }
    }

    private def writeDockerPort(dockerPort: DockerPort): JsValue = {
      if (dockerPort.hostPort.isDefined) {
        s"${dockerPort.containerPort} -> ${dockerPort.hostPort.get}".toJson
      } else {
        s"${dockerPort.containerPort}".toJson
      }
    }

    private def readDockerPort(jsonStr: String): DockerPort = {
      val ports: Array[Int] = jsonStr.split("->").map(_.trim.toInt)
      ports.tail.headOption
      if (ports.length == 2) {
        DockerPort(ports(0), Some(ports(1)))
      } else if (ports.length == 1) {
        DockerPort(ports(0), None)
      } else {
        throw deserializationError(s"Cannot deserialize DockerEntity: invalid input. Raw input: $jsonStr")
      }
    }
  }

  implicit val dockerEntityProtocol: RootJsonFormat[DockerEntity] = new RootJsonFormat[DockerEntity] {
    private val IMAGE = "image"
    private val ROLE = "role"
    private val NETWORK = "network"
    private val RESOURCE = "resource"
    private val ARGUMENTS = "arguments"

    private val DEFAULT_ROLE = "*"
    private val DEFAULT_NETWORK = "bridge"

    override def write(dockerEntity: DockerEntity): JsValue = {
      JsObject(List(
        Some(IMAGE -> dockerEntity.image.toJson),
        Some(ROLE -> dockerEntity.role.toJson),
        Some(NETWORK -> dockerEntity.network.toJson),
        Some(RESOURCE -> dockerEntity.resource.toJson),
        Some(ARGUMENTS -> dockerEntity.arguments.toJson)
      ).flatten: _*)
    }

    override def read(json: JsValue): DockerEntity = {
      val jsObject = json.asJsObject
      val role = jsObject.fields.get(ROLE).map(_.convertTo[String]).getOrElse(DEFAULT_ROLE).trim
      val network = jsObject.fields.get(NETWORK).map(_.convertTo[String]).getOrElse(DEFAULT_NETWORK).trim
      val arguments = jsObject.fields.get(ARGUMENTS).map(_.convertTo[List[String]]).getOrElse(List())
      jsObject.getFields(IMAGE, RESOURCE) match {
        case Seq(imageJs, resourceJs) =>
          val image = imageJs.convertTo[String]
          val resource = resourceJs.convertTo[DockerResource]
          DockerEntity(image, role, network, resource, arguments)
        case other => deserializationError(s"Cannot deserialize DockerEntity: invalid input. Raw input: $other")
      }
    }
  }

  implicit val rawBeanProtocol: RootJsonFormat[RawBean] = jsonFormat4(RawBean)
  implicit val quantityBeanProtocol: RootJsonFormat[QuantityBean] = jsonFormat3(QuantityBean)
  implicit val packProtocol: RootJsonFormat[Pack] = jsonFormat3(Pack)

  implicit val beanProtocol: RootJsonFormat[Bean] = new RootJsonFormat[Bean] {
    private val HOSTNAME = "hostname"
    private val AGENT_ID = "agentId"
    private val TASK_ID = "taskId"
    private val NAME = "name"
    private val DOCKER_ENTITY = "dockerEntity"
    private val CHECKS = "checks"

    override def write(bean: Bean): JsValue =
      JsObject(List(
        Some(TASK_ID -> bean.taskId.toJson),
        Some(NAME -> bean.name.toJson),
        Some(DOCKER_ENTITY -> bean.dockerEntity.toJson),
        bean.hostname.map(hostname => HOSTNAME -> hostname.toJson),
        bean.agentId.map(agentId => AGENT_ID -> agentId.toJson),
        Some(CHECKS -> bean.checks.toJson)
      ).flatten: _*)

    override def read(json: JsValue): Bean = {
      val jsObject = json.asJsObject
      val agentIdOpt = jsObject.fields.get(AGENT_ID).map(_.convertTo[String])
      val hostnameOpt = jsObject.fields.get(HOSTNAME).map(_.convertTo[String])
      val checks = jsObject.fields.get(CHECKS).map(_.convertTo[List[BeanCheck]]).getOrElse(List())
      jsObject.getFields(TASK_ID, NAME, DOCKER_ENTITY) match {
        case Seq(taskIdRaw, nameJs, dockerEntityJs) =>
          val dockerEntity = dockerEntityJs.convertTo[DockerEntity]
          val rawTask = taskIdRaw.convertTo[String]
          val name = nameJs.convertTo[String]
          val fullTaskIdRegex = s"""(.*)~$name~(\\d+)""".r
          val partialTaskIdRegex = s"""$name~(\\d+)""".r
          rawTask match {
            case fullTaskIdRegex(pack, taskId) => Bean(id = taskId, name = name, dockerEntity = dockerEntity, pack = Some(pack), agentId = agentIdOpt, hostname = hostnameOpt, checks = checks)
            case partialTaskIdRegex(taskId) => Bean(id = taskId, name = name, dockerEntity = dockerEntity, pack = None, agentId = agentIdOpt, hostname = hostnameOpt, checks = checks)
            case _ => Bean(id = rawTask, name = name, dockerEntity = dockerEntity, agentId = agentIdOpt, hostname = hostnameOpt, checks = checks) // todo this should be logged, not normal
          }
        case other => deserializationError(s"Cannot deserialize Bean: invalid input. Raw input: $other")
      }
    }
  }

  implicit val offerProtocol: RootJsonFormat[Offer] = new RootJsonFormat[Offer] {
    private val ID = "id"
    private val AGENT_ID = "agentId"
    private val HOSTNAME = "hostname"
    private val MEM = "mem"
    private val CPU = "cpu"
    private val PORTS = "ports"
    private val START = "start"
    private val END = "end"

    override def write(offer: Offer): JsValue =
      JsObject(Map(
        ID -> offer.id.toJson,
        AGENT_ID -> offer.agentId.toJson,
        HOSTNAME -> offer.hostname.toJson,
        MEM -> offer.mem.toJson,
        CPU -> offer.cpu.toJson,
        PORTS -> offer.ports.map(r => Map(START -> r.start, END -> r.end).toJson).toJson
      ))


    override def read(json: JsValue): Offer = {
      val jsObject = json.asJsObject
      jsObject.getFields(ID, AGENT_ID, HOSTNAME, MEM, CPU, PORTS) match {
        case Seq(idJs, agentIdJs, hostnameJs, memJs, cpuJsJs, portsJs) => {
          val id = idJs.convertTo[String]
          val agentId = agentIdJs.convertTo[String]
          val hostname = hostnameJs.convertTo[String]
          val mem = memJs.convertTo[Double]
          val cpu = cpuJsJs.convertTo[Double]
          val ports: List[Range.Inclusive] = portsJs.convertTo[List[JsValue]].map(_.asJsObject.getFields(START, END) match {
            case Seq(startJs, endJs) => Range.inclusive(startJs.convertTo[Int], endJs.convertTo[Int])
            case other => deserializationError(s"Cannot deserialize Offer: invalid input. Raw input: $other")
          })

          Offer(id, agentId, hostname, mem, cpu, ports)

        }
        case other => deserializationError(s"Cannot deserialize Offer: invalid input. Raw input: $other")
      }
    }
  }


}
