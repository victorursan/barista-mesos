package com.victorursan.mesos

import java.util.function.Function
import java.net.URI

import com.google.protobuf.ByteString
import com.mesosphere.mesos.rx.java.util.UserAgentEntry
import org.apache.mesos.v1.Protos.Offer.Operation
import org.apache.mesos.v1.Protos._
import org.apache.mesos.v1.scheduler.Protos.Call.Reconcile

/**
  * Created by victor on 4/10/17.
  */
trait MesosSchedulerCalls {
  //  @throws[URISyntaxException]
  def subscribe(mesosMaster: URI, frameworkName: String, failoverTimeout: Double, mesosRole: String, applicationUserAgentEntry: Function[Class[_], UserAgentEntry], frameworkId: String): Unit

  def teardown(): Unit

  def accept(offerIds: List[OfferID], offerOperations: List[Operation], filtersOpt: Option[Filters] = None): Unit

  def decline(offerIds: List[OfferID], filtersOpt: Option[Filters] = None): Unit

  def acceptInverseOffers(offerIds: List[OfferID], filtersOpt: Option[Filters] = None): Unit

  def declineInverseOffers(offerIds: List[OfferID], filtersOpt: Option[Filters] = None): Unit

  def kill(taskId: TaskID, agentIdOpt: Option[AgentID] = None, killPolicyOpt: Option[KillPolicy] = None): Unit

  def revive(): Unit

  def shutdown(executorId: ExecutorID, agentIdOpt: Option[AgentID] = None): Unit

  def acknowledge(agentId: AgentID, taskId: TaskID, uuid: ByteString): Unit

  def reconsile(tasks: List[Reconcile.Task]): Unit

  def message(agentId: AgentID, executorId: ExecutorID, data: ByteString): Unit

  def request(requests: List[Request]): Unit

  def close(): Unit
}
