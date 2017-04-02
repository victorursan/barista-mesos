package com.victorursan.services

import spray.json.DefaultJsonProtocol

/**
  * Created by victor on 4/2/17.
  */

case class Status(uptime: String)

trait Protocol extends DefaultJsonProtocol {
  implicit val statusFormatter = jsonFormat1(Status.apply)
}
