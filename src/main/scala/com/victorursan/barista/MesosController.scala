package com.victorursan.barista

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.victorursan.state.AgentResources
import com.victorursan.utils.{JsonSupport, MesosConf}
import com.victorursan.zookeeper.StateController
import org.apache.mesos.v1.Protos.AgentID
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

object MesosController extends MesosConf with JsonSupport {
  private implicit val system: ActorSystem = ActorSystem("Barista-docker-actor-system")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val ec: ExecutionContext = system.dispatcher
  private val orig = ConnectionPoolSettings(system.settings.config).copy(idleTimeout = 10 seconds)
  private val clientSettings = orig.connectionSettings.withIdleTimeout(10 seconds)
  private val settings = orig.copy(connectionSettings = clientSettings)


  def checkAgents(agentIds: List[AgentID]): Map[String, AgentResources] = {
    val savedAgents = StateController.agentResources
    if (agentIds.forall(agentId => savedAgents.keySet.contains(agentId.getValue))) {
      savedAgents
    } else {
      StateController.updateAgentResources(agentResources())
    }
  }

  private def agentResources(): Map[String, AgentResources] = {
    val eventualMap: Future[Map[String, AgentResources]] = Await.result(
      Http()
        .singleRequest(HttpRequest(HttpMethods.PUT, Uri.from(scheme = mesosUri.getScheme, host = mesosUri.getHost,
          port = mesosUri.getPort, path = "/master/slaves"), entity = HttpEntity.Empty), settings = settings), 10 seconds)
      .entity.dataBytes.runFold(Map[String, AgentResources]())((c: Map[String, AgentResources], chunk: ByteString) => c ++ getAgentResources(chunk.utf8String))
    Await.result(eventualMap, 10 seconds)
  }

  def getAgentResources(data: String): Map[String, AgentResources] = {
    val allJs = data.parseJson.asJsObject.convertTo[Map[String, JsValue]]
    val slavesJs = allJs("slaves").convertTo[List[JsValue]]
    slavesJs.map(getAgentResource).reduce(_ ++ _)
  }

  def getAgentResource(agentJs: JsValue): Map[String, AgentResources] = {
    val slaveJs = agentJs.convertTo[Map[String, JsValue]]
    val resourcesJs = slaveJs("resources").convertTo[Map[String, JsValue]]

    val id: String = slaveJs("id").convertTo[String]
    val disk: Double = resourcesJs("disk").convertTo[Double]
    val mem: Double = resourcesJs("mem").convertTo[Double]
    val cpus: Double = resourcesJs("cpus").convertTo[Double]

    Map(id -> AgentResources(mem, cpus, disk))
  }
}

