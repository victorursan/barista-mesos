package com.victorursan.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.victorursan.state.{Bean, DockerEntity, DockerResource, RawBean}
import spray.json._

/**
  * Created by victor on 4/2/17.
  */
trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val resourceProtocol: RootJsonFormat[DockerResource] = jsonFormat2(DockerResource)
  implicit val dockerServiceProtocol: RootJsonFormat[DockerEntity] = jsonFormat3(DockerEntity)
  implicit val rawBeanProtocol: RootJsonFormat[RawBean] = jsonFormat2(RawBean)
  implicit val beanProtocol: RootJsonFormat[Bean] = new RootJsonFormat[Bean] {
    private val OFFER_ID = "offerId"
    private val AGENT_ID = "agentId"
    private val TASK_ID = "taskId"
    private val NAME = "name"
    private val DOCKER_ENTITY = "dockerEntity"

    override def write(bean: Bean): JsValue = {
      JsObject(List(
        Some(TASK_ID -> bean.taskId.toJson),
        Some(NAME -> bean.name.toJson),
        Some(DOCKER_ENTITY -> bean.dockerEntity.toJson),
        bean.offerId.map(offerId => OFFER_ID -> offerId.toJson),
        bean.agentId.map(agentId => AGENT_ID -> agentId.toJson)
      ).flatten: _*)
    }

    override def read(json: JsValue): Bean = {
      val jsObject = json.asJsObject
      val agentIdOpt = jsObject.fields.get(AGENT_ID).map(_.convertTo[String])
      val offerIdOpt = jsObject.fields.get(OFFER_ID).map(_.convertTo[String])
      jsObject.getFields(TASK_ID, NAME, DOCKER_ENTITY) match {
        case Seq(taskIdRaw, nameJs, dockerEntityJs) => {
          val dockerEntity = dockerEntityJs.convertTo[DockerEntity]
          val rawTask = taskIdRaw.convertTo[String]
          val name = nameJs.convertTo[String]
          val fullTaskIdRegex = s"""(.*)~$name~(\\d+)""".r
          val partialTaskIdRegex = s"""$name~(\\d+)""".r
          rawTask match {
            case fullTaskIdRegex(pack, taskId) => Bean(id = taskId, name = name, dockerEntity = dockerEntity, pack = Some(pack), agentId = agentIdOpt, offerId = offerIdOpt)
            case partialTaskIdRegex(taskId) => Bean(id = taskId, name = name, dockerEntity = dockerEntity, pack = None, agentId = agentIdOpt, offerId = offerIdOpt)
            case _ => Bean(id = rawTask, name = name, dockerEntity = dockerEntity, agentId = agentIdOpt, offerId = offerIdOpt) // todo this should be logged, not normal
          }
        }
        case other => deserializationError("Cannot deserialize Bean: invalid input. Raw input: " + other)
      }
    }
  }
}
