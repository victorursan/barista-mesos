package com.victorursan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.victorursan.services.BaristaService
import com.victorursan.utils.Config

/**
  * Created by victor on 4/2/17.
  */
object MainService extends App with BaristaService with Config {
  override protected implicit val system: ActorSystem = ActorSystem()
  override protected implicit val materializer: ActorMaterializer = ActorMaterializer()

  Http().bindAndHandle(routes, httpInterface, httpPort)
}
