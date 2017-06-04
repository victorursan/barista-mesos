package com.victorursan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.victorursan.services.BaristaService
import com.victorursan.utils.HttpConfig
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Created by victor on 4/2/17.
  */
object MainService extends App with HttpConfig with BaristaService {
  override protected implicit val system: ActorSystem = ActorSystem("Barista-service-actor-system")
  override protected implicit val materializer: ActorMaterializer = ActorMaterializer()
  override protected implicit val ec: ExecutionContext = system.dispatcher
  private val log = LoggerFactory.getLogger(MainService.getClass)

  Http().bindAndHandle(routes, httpInterface, httpPort)
    .onComplete {
      case Success(s) => log.info(s.toString)
      case Failure(t) => log.error(s"Failed to bind to $httpInterface:$httpPort!", t)
    }

}
