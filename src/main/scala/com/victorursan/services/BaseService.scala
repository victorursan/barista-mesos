package com.victorursan.services

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import com.victorursan.utils.JsonSupport

import scala.concurrent.ExecutionContext

/**
  * Created by victor on 4/2/17.
  */
trait BaseService extends Protocol with Directives with JsonSupport {
  protected def serviceName: String

  protected def system: ActorSystem

  protected def materializer: ActorMaterializer

  protected def ec: ExecutionContext
}
