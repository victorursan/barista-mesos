package com.victorursan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.victorursan.services.BaristaService
import org.apache.mesos.Protos.FrameworkInfo

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App with BaristaService {
  override protected implicit val system: ActorSystem = ActorSystem()
  override protected implicit val materializer: ActorMaterializer = ActorMaterializer()

  Http().bindAndHandle(routes, httpInterface, httpPort)
}