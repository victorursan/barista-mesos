package com.victorursan.mesos

import java.net.URI
import java.util.function.Function

import com.google.protobuf.ByteString
import com.mesosphere.mesos.rx.java.util.UserAgentEntry
import com.victorursan.state.Bean
import org.apache.mesos.v1.Protos
import org.apache.mesos.v1.Protos.Offer.Operation
import org.apache.mesos.v1.Protos._
import org.apache.mesos.v1.scheduler.Protos.Call.Reconcile

/**
  * Created by victor on 4/10/17.
  */
trait MesosSchedulerCalls {
  def subscribe(mesosMaster: URI, frameworkName: String, failoverTimeout: Double, mesosRole: String, applicationUserAgentEntry: Function[Class[_], UserAgentEntry], frameworkId: String): Unit

  def teardown(): Unit

  def acceptContainer(bean: Bean, offerId: String, filtersOpt: Option[Protos.Filters] = None): Unit

  def accept(offerIds: Iterable[OfferID], offerOperations: List[Operation], filtersOpt: Option[Filters] = None): Unit

  def decline(offerIds: Iterable[OfferID], filtersOpt: Option[Filters] = None): Unit

  def acceptInverseOffers(offerIds: List[OfferID], filtersOpt: Option[Filters] = None): Unit

  def declineInverseOffers(offerIds: List[OfferID], filtersOpt: Option[Filters] = None): Unit

  def kill(taskId: TaskID, agentIdOpt: Option[AgentID] = None, killPolicyOpt: Option[KillPolicy] = None): Unit

  def revive(): Unit

  def shutdown(executorId: ExecutorID, agentIdOpt: Option[AgentID] = None): Unit

  def acknowledge(agentId: AgentID, taskId: TaskID, uuid: ByteString): Unit

  def reconsile(tasks: Iterable[Reconcile.Task]): Unit

  def message(agentId: AgentID, executorId: ExecutorID, data: ByteString): Unit

  def request(requests: List[Request]): Unit

  def close(): Unit
}
