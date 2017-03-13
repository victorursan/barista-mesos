package com.victorursan

import java.util.concurrent.atomic.AtomicInteger

import com.mesosphere.mesos.rx.java.protobuf.ProtoUtils.protoToString
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

/**
  * Created by victor on 3/8/17.
  */
case class State[FwId, TaskId, TaskState](fwId: FwId,containerName: String, containerImage: String, resourceRole: String, cpusPerTask: Double, memMbPerTask: Double) {
  private val LOGGER = LoggerFactory.getLogger(classOf[State[_, _, _]])

  var taskState: TrieMap[TaskId, TaskState] = TrieMap[TaskId, TaskState]()
  val offerCounter: AtomicInteger = new AtomicInteger
  val totalTaskCounter: AtomicInteger = new AtomicInteger

  def put(key: TaskId, value: TaskState) {
    LOGGER.debug("put(key : {}, value : {})", protoToString(key), value)
    taskState += (key -> value)
  }
}
