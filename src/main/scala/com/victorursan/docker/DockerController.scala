package com.victorursan.docker

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.stream.ActorMaterializer
import com.victorursan.state.BeanDocker
import com.victorursan.utils.JsonSupport
import rx.lang.scala.subjects.SerializedSubject
import rx.lang.scala.{Observable, Subject}
import spray.json._

import scala.concurrent.ExecutionContext

/**
  * Created by victor on 5/28/17.
  */
object DockerController extends JsonSupport {

  private implicit val system: ActorSystem = ActorSystem("Barista-docker-actor-system")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val ec: ExecutionContext = system.dispatcher

  def registerBeanDocker(beanDocker: BeanDocker): Observable[DockerStatus] = {
    val beanBuss = Subject[DockerStatus]()
    val threadSafeBeanBuss: SerializedSubject[DockerStatus] = SerializedSubject[DockerStatus](beanBuss)

    val request: HttpRequest = HttpRequest(HttpMethods.GET, uri = Uri.from(scheme = "http", host = beanDocker.hostname, port = 2375, path = s"/containers/${beanDocker.dockerId}/stats"))
    Http().singleRequest(request)
      .flatMap(response => {
        response.entity.dataBytes.runForeach { chunk =>
          val stat = getContainerStatus(beanDocker.taskId, chunk.utf8String)
          threadSafeBeanBuss.onNext(stat)
        }
      })
    threadSafeBeanBuss

  }

  def getContainerStatus(taskId: String, chunk: String): DockerStatus = {
    val chunkJs = chunk.parseJson.convertTo[Map[String, JsValue]]
    val cPUStats = chunkJs("cpu_stats").convertTo[Map[String, JsValue]]
    val cPUStatsCPUUsage = cPUStats("cpu_usage").convertTo[Map[String, JsValue]]
    val preCPUStats = chunkJs("precpu_stats").convertTo[Map[String, JsValue]]
    val preCPUStatsCPUUsage = preCPUStats("cpu_usage").convertTo[Map[String, JsValue]]
    val cpuTotalUsage = cPUStatsCPUUsage("total_usage").convertTo[Double]
    val preCpuTotalUsage = preCPUStatsCPUUsage("total_usage").convertTo[Double]
    val cpuSystem = cPUStats("system_cpu_usage").convertTo[Double]
    val preCpuSystem = preCPUStats.get("system_cpu_usage").map(_.convertTo[Double]).orElse(Some(cpuSystem - 100)).get
    val nrCpu = cPUStatsCPUUsage("percpu_usage").convertTo[List[Double]].size
    val memStats = chunkJs("memory_stats").convertTo[Map[String, JsValue]]
    val memUsed = memStats("usage").convertTo[Double]
    val statsMem = memStats("stats").convertTo[Map[String, JsValue]]
    val maxMem = statsMem("hierarchical_memory_limit").convertTo[Double]
    val cpuDelta: Double = cpuTotalUsage - preCpuTotalUsage
    // CPUStats.CPUUsage.TotalUsage - PreCPUStats.CPUUsage.TotalUsage
    val systemDelta: Double = cpuSystem - preCpuSystem
    // CPUStats.SystemUsage - PreCPUStats.SystemUsage
    val cpuPercent = (cpuDelta / systemDelta) * nrCpu * 100

    DockerStatus(taskId = taskId, cpuPer = cpuPercent, memUsed / maxMem * 100, memUsed, maxMem)
  }

}
