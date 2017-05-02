package com.victorursan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.victorursan.services.BaristaService
import com.victorursan.utils.Config

/**
  * Created by victor on 4/2/17.
  */
object MainService extends App with Config with BaristaService  {
  override protected implicit val system: ActorSystem = ActorSystem("Barista-actor-system")
  override protected implicit val materializer: ActorMaterializer = ActorMaterializer()

  Http().bindAndHandle(routes, httpInterface, httpPort)
}
