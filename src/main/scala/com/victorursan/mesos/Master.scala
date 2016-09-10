package com.victorursan.mesos

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.slf4j.LoggerFactory

/**
  * Created by victor on 9/10/16.
  */
object Master {
  protected val serviceName = "Master"
  implicit val system = ActorSystem(serviceName)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  private val log = LoggerFactory.getLogger(serviceName)

  //  def registerScheduler()(implicit leaderUri: Uri): Future[HttpResponse] = {
  //    log.info("Registering scheduler.")
  //    val httpRequest = HttpRequest(uri = leaderUri,
  //      method = GET,
  //      headers = List(Accept(MediaRange(MediaTypes.`application/json`))))
  //    log.info(s"$httpRequest")
  //    Http().singleRequest(httpRequest)
  //  }
  //
  //  def teardownScheduler()(implicit leaderUri: Uri): Future[HttpResponse] = {
  //    log.info("Teardown scheduler.")
  //    val httpRequest = HttpRequest(uri = leaderUri,
  //      method = GET,
  //      headers = List(Accept(MediaRange(MediaTypes.`application/json`))))
  //    log.info(s"$httpRequest")
  //    Http().singleRequest(httpRequest)
  //  }

}
