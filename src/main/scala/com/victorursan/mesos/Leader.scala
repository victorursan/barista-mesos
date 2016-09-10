package com.victorursan.mesos

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.language.postfixOps

/**
  * Created by victor on 8/31/16.
  */
object Leader {
  protected val serviceName = "Leader"
  implicit val system = ActorSystem(serviceName)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  private val log = LoggerFactory.getLogger(serviceName)

  def get(masterUri: Uri = "http://10.1.1.11:5050/redirect", leaderHeader: String = "Location"): Future[Uri] = {
    log.info("Searching for leader.")
    val httpRequest = HttpRequest(uri = masterUri,
      method = GET,
      headers = List(Accept(MediaRange(MediaTypes.`application/json`))))
    log.info(s"$httpRequest")
    val responseFuture1: Future[HttpResponse] = Http().singleRequest(httpRequest)
    responseFuture1.map { httpResponse => {
      val httpStringResponse: Array[String] = httpResponse.getHeader(leaderHeader).get.value.stripPrefix("//").split(":")
      val ip = httpStringResponse.head
      val port = httpStringResponse.tail.head.toInt
      Uri(ip).withPort(port)
    }
    }
  }
}
