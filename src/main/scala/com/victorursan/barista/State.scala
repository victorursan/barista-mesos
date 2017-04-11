package com.victorursan.barista

import java.util.concurrent.atomic.AtomicInteger

import com.mesosphere.mesos.rx.java.protobuf.ProtoUtils.protoToString
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

/**
  * Created by victor on 3/8/17.
  */
case class State[FwId, TaskId, TaskState](frameworkId: FwId, dockerEntity: DockerEntity) {
  private val log = LoggerFactory.getLogger(classOf[State[_, _, _]])

  private val dockerEntities: List[DockerEntity] = List[DockerEntity]()
  val offerCounter: AtomicInteger = new AtomicInteger
  val totalTaskCounter: AtomicInteger = new AtomicInteger
  var taskState: TrieMap[TaskId, TaskState] = TrieMap[TaskId, TaskState]()

  def put(key: TaskId, value: TaskState) {
    log.debug("put(key : {}, value : {})", protoToString(key), value)
    taskState += (key -> value)
  }


}
