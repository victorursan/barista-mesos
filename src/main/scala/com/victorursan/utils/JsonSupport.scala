package com.victorursan.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.victorursan.state.{Bean, DockerEntity, DockerResource}
//import spray.json.{DefaultJsonProtocol, JsObject, JsValue, RootJsonFormat, deserializationError, }
import spray.json._

/**
  * Created by victor on 4/2/17.
  */
trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val resourceProtocol: RootJsonFormat[DockerResource] = jsonFormat2(DockerResource)
  implicit val dockerServiceProtocol: RootJsonFormat[DockerEntity] = jsonFormat4(DockerEntity)
  implicit val beanProtocol: RootJsonFormat[Bean] = new RootJsonFormat[Bean] {
    private val OFFER_ID = "offerId"
    private val AGENT_ID = "agentId"
    private val TASK_ID = "taskId"
    private val DOCKER_ENTITY = "dockerEntity"

    override def write(bean: Bean): JsValue = {
      val taskId = bean.pack match {
        case Some(pack) => s"$pack~${bean.dockerEntity.name}~${bean.taskId}"
        case None => s"${bean.dockerEntity.name}~${bean.taskId}"
      }
      JsObject(List(
        Some(TASK_ID -> taskId.toJson),
        Some(DOCKER_ENTITY -> bean.dockerEntity.toJson),
        bean.offerId.map(offerId => OFFER_ID -> offerId.toJson),
        bean.agentId.map(agentId => AGENT_ID -> agentId.toJson)
      ).flatten: _*)
    }

    override def read(json: JsValue): Bean = {
      val jsObject = json.asJsObject
      val agentIdOpt = jsObject.fields.get(AGENT_ID).map(_.convertTo[String])
      val offerIdOpt = jsObject.fields.get(OFFER_ID).map(_.convertTo[String])
      jsObject.getFields(TASK_ID, DOCKER_ENTITY) match {
        case Seq(taskIdRaw, dockerEntityJs) => {
          val dockerEntity = dockerEntityJs.convertTo[DockerEntity]
          val rawTask = taskIdRaw.convertTo[String]
          val fullTaskIdRegex = s"""(.*)~${dockerEntity.name}~(\\d+)""".r
          val partialTaskIdRegex = s"""${dockerEntity.name}~(\\d+)""".r
          rawTask match {
            case fullTaskIdRegex(pack, taskId) => Bean(taskId, dockerEntity, Some(pack), agentIdOpt, offerIdOpt)
            case partialTaskIdRegex(taskId) => Bean(taskId, dockerEntity)
            case _ => Bean(rawTask, dockerEntity) // todo this should be logged, not normal
          }

        }
        case other => deserializationError("Cannot deserialize Bean: invalid input. Raw input: " + other)
      }
    }
  }
}
