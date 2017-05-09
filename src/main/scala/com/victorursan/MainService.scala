package com.victorursan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import com.victorursan.services.BaristaService
import com.victorursan.utils.Config
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
/**
  * Created by victor on 4/2/17.
  */
object MainService extends App with Config with BaristaService  {
  override protected implicit val system: ActorSystem = ActorSystem("Barista-actor-system")
  override protected implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val log = LoggerFactory.getLogger(MainService.getClass)

  val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(routes, httpInterface, httpPort)

  bindingFuture.onComplete  {
    case Success(s) => log.info(s.toString)
    case Failure(t) => log.error(s"Failed to bind to $httpInterface:$httpPort!", t)
  } (materializer.executionContext)
}
