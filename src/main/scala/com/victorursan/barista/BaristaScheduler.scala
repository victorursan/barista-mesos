package com.victorursan.barista

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import com.victorursan.mesos.Leader
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._

object BaristaScheduler {
  protected val serviceName = "BaristaScheduler"
  implicit val system = ActorSystem(serviceName)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  private val log = LoggerFactory.getLogger(serviceName)

  implicit val leaderUri: Uri = Await.result(Leader.get(), 10 seconds)

  //  def register(): Future[HttpResponse] = Master.registerScheduler()
}
