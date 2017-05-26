package com.victorursan.docker
import scala.util.{ Failure, Success }
import scala.concurrent.{ Future, Promise }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import akka.stream.{ OverflowStrategy, QueueOfferResult }

import scala.concurrent.Promise
import spray.json._
/**
  * Created by victor on 5/18/17.
  */
object DockerMain extends App {



//  implicit val system = ActorSystem()
//  import system.dispatcher // to get an implicit ExecutionContext into scope
//  implicit val materializer = ActorMaterializer()
//
//  val QueueSize = 10
//
//  // This idea came initially from this blog post:
//  // http://kazuhiro.github.io/scala/akka/akka-http/akka-streams/2016/01/31/connection-pooling-with-akka-http-and-source-queue.html
//  val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]]("10.1.10.11", port=2375)
//  val queue =
//    Source.queue[(HttpRequest, Promise[HttpResponse])](QueueSize, OverflowStrategy.dropNew)
//      .via(poolClientFlow)
//      .toMat(Sink.foreach({
//        case ((Success(resp), p)) => p.success(resp)
//        case ((Failure(e), p))    => p.failure(e)
//      }))(Keep.left)
//      .run()
//
//  def queueRequest(request: HttpRequest): Future[HttpResponse] = {
//    val responsePromise = Promise[HttpResponse]()
//    queue.offer(request -> responsePromise).flatMap {
//      case QueueOfferResult.Enqueued    => responsePromise.future
//      case QueueOfferResult.Dropped     => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
//      case QueueOfferResult.Failure(ex) => Future.failed(ex)
//      case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
//    }
//  }
//
//  val responseFuture: Future[HttpResponse] = queueRequest(HttpRequest(uri = "/containers/79a9e274fae0b44d9360c46aa129c95df8b0a3285cb585697fd3c8ea76cd555d/stats"))
//  responseFuture.onComplete {
//    case Success(s) => print("yes" + s.entity)
//  }
////  val pinger = system.actorOf(Props(new TcpClient( new  InetSocketAddress("http://10.1.10.11:2375/containers/79a9e274fae0b44d9360c46aa129c95df8b0a3285cb585697fd3c8ea76cd555d/stats", 2375), "", promise)), "pinger")
////  new TcpClient(new InetSocketAddress("http://10.1.10.11", 2375), "containers/79a9e274fae0b44d9360c46aa129c95df8b0a3285cb585697fd3c8ea76cd555d/stats", promise)
//  val cpuDelta: Double = 9504372473l - 9502756562l// CPUStats.CPUUsage.TotalUsage - PreCPUStats.CPUUsage.TotalUsage
//  val systemDelta: Double = 4071030000000l - 4070030000000l// CPUStats.SystemUsage - PreCPUStats.SystemUsage
//  val cpuPercent = (cpuDelta / systemDelta) * 1 * 100// float64(len(v.CPUStats.CPUUsage.PercpuUsage)) * 100.0
//
//  print(cpuPercent)
}
//http://10.1.10.11:2375/containers/mesos-034343aa-3138-474a-b0ae-1e3a3a201927-S0.689288ed-fd8b-4c5a-8088-e53a5b452144/stats