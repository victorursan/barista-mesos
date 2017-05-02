package com.victorursan.barista

import java.net.URI
import java.util
import java.util.{Optional, UUID}

import com.google.protobuf.ByteString
import com.mesosphere.mesos.rx.java.SinkOperations.sink
import com.mesosphere.mesos.rx.java.protobuf.SchedulerCalls.decline
import com.mesosphere.mesos.rx.java.util.UserAgentEntries
import com.mesosphere.mesos.rx.java.{AwaitableSubscription, SinkOperation, SinkOperations}
import com.victorursan.state.{Bean, DockerEntity}
import com.victorursan.zookeeper.StateController
import org.apache.mesos.v1.Protos
import org.apache.mesos.v1.Protos._
import org.apache.mesos.v1.scheduler.Protos.Call
import org.apache.mesos.v1.scheduler.Protos.Call.{Acknowledge, Decline, Type}
import org.slf4j.LoggerFactory
import rx.lang.scala.Subject
import rx.lang.scala.subjects.ReplaySubject
import rx.subjects.SerializedSubject

import scala.collection.JavaConverters._

/**
  * Created by victor on 4/2/17.
  */
class BaristaController {
  private val fwName = "Barista"
  private val fwId = s"$fwName-${UUID.randomUUID}"
  private val mesosUri = URI.create("http://localhost:8000/mesos/api/v1/scheduler")
  private val role = "*"
  //  val baristaCalls = new BaristaCalls

  def start(): Unit = {
    BaristaCalls.subscribe(mesosUri, fwName, 10, role, UserAgentEntries.literal("com.victorursan", "barista"), fwId)
  }



}
