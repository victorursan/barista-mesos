package com.victorursan.barista

import java.util.Optional

import com.google.protobuf.ByteString
import com.mesosphere.mesos.rx.java.protobuf.{ProtobufMesosClientBuilder, SchedulerCalls}
import com.mesosphere.mesos.rx.java.util.UserAgentEntries
import com.mesosphere.mesos.rx.java.{AwaitableSubscription, SinkOperation, SinkOperations}
import com.victorursan.mesos.MesosSchedulerCalls
import com.victorursan.state.Bean
import com.victorursan.utils.MesosConf
import org.apache.mesos.v1.Protos
import org.apache.mesos.v1.Protos.{AgentID, FrameworkID, Offer, OfferID}
import org.apache.mesos.v1.scheduler.Protos.Call.Type._
import org.apache.mesos.v1.scheduler.Protos.Call.{Accept, AcceptInverseOffers, Acknowledge, Decline, DeclineInverseOffers, Kill, Message, Reconcile, Request, Shutdown}
import org.apache.mesos.v1.scheduler.Protos.{Call, Event}
import rx.lang.scala.JavaConversions.toScalaObservable
import rx.lang.scala.Observable
import rx.subjects.{PublishSubject, SerializedSubject}

import scala.collection.JavaConverters._

/**
  * Created by victor on 4/10/17.
  */
object BaristaCalls extends MesosSchedulerCalls with MesosConf{
  private val publishSubject: SerializedSubject[Optional[SinkOperation[Call]], Optional[SinkOperation[Call]]] = PublishSubject.create[Optional[SinkOperation[Call]]]().toSerialized
  private var frameworkID = FrameworkID.newBuilder
    .setValue(frameworkId)
    .build()
  private var openStream: AwaitableSubscription = null

  override def subscribe(): Unit = {
    val clientBuilder = ProtobufMesosClientBuilder.schedulerUsingProtos
      .mesosUri(mesosUri)
      .applicationUserAgentEntry(UserAgentEntries.literal(userAEName, userAEVersion))
    val subscribeCall: Call = SchedulerCalls.subscribe(
      frameworkID,
      Protos.FrameworkInfo.newBuilder()
        .setId(frameworkID)
        .setUser(user)
        .setName(frameworkName)
        .setFailoverTimeout(failoverTimeout)
        .build())



    openStream = clientBuilder
      .subscribe(subscribeCall)
      .processStream { case (unicastEvents: rx.Observable[Event]) =>
        val events: Observable[Event] = toScalaObservable(unicastEvents.share())

        events.filter(_.getType == Event.Type.ERROR)
          .subscribe { e: Event => BaristaCallbacks.receivedError(e.getError) }

        events.filter(_.getType == Event.Type.FAILURE)
          .subscribe { e: Event => BaristaCallbacks.receivedFailure(e.getFailure) }

        events.filter(_.getType == Event.Type.HEARTBEAT)
          .subscribe { _ => BaristaCallbacks.receivedHeartbeat() }

        events.filter(_.getType == Event.Type.INVERSE_OFFERS)
          .subscribe { e: Event => BaristaCallbacks.receivedInverseOffers(e.getInverseOffers.getInverseOffersList.asScala.toList) }

        events.filter(_.getType == Event.Type.MESSAGE)
          .subscribe { e: Event => BaristaCallbacks.receivedMessage(e.getMessage) }

        events.filter(_.getType == Event.Type.OFFERS)
          .subscribe { e: Event => BaristaCallbacks.receivedOffers(e.getOffers.getOffersList.asScala.toList) }

        events.filter(_.getType == Event.Type.RESCIND)
          .subscribe { e: Event => BaristaCallbacks.receivedRescind(e.getRescind.getOfferId) }

        events.filter(_.getType == Event.Type.RESCIND_INVERSE_OFFER)
          .subscribe { e: Event => BaristaCallbacks.receivedRescindInverseOffer(e.getRescindInverseOffer.getInverseOfferId) }

        events.filter(_.getType == Event.Type.SUBSCRIBED)
          .subscribe { e: Event =>
            frameworkID = e.getSubscribed.getFrameworkId
            BaristaCallbacks.receivedSubscribed(e.getSubscribed)
          }

        events.filter((event: Event) => event.getType == Event.Type.UPDATE)
          .subscribe((e: Event) => {
            val status = e.getUpdate.getStatus
            // Per mesos protocol, if we get an update and its UUID exist,
            // then we need to call acknowledge.
            if (!status.getUuid.isEmpty) {
              acknowledge(status.getAgentId, status.getTaskId, status.getUuid)
            }
            BaristaCallbacks.receivedUpdate(e.getUpdate.getStatus)
          })

        publishSubject
      }
      .build
      .openStream

    try {
      openStream.await()
    } catch {
      case t: Throwable => t.printStackTrace()
    }
  }

  override def acknowledge(agentId: Protos.AgentID, taskId: Protos.TaskID, uuid: ByteString): Unit =
    sendCall(Call.newBuilder()
      .setAcknowledge(Acknowledge.newBuilder
        .setAgentId(agentId)
        .setTaskId(taskId)
        .setUuid(uuid)), ACKNOWLEDGE)

  def acceptContainer(bean: Bean, offerId: String, filtersOpt: Option[Protos.Filters] = None): Unit =
    accept(List(createOfferId(offerId)), List(
      Offer.Operation.newBuilder()
        .setType(Offer.Operation.Type.LAUNCH)
        .setLaunch(
          Offer.Operation.Launch.newBuilder
            .addAllTaskInfos(List(createAgentId(bean.agentId.get)).map(TaskHandler.createTaskWith(_, bean)).asJava)) //todo remove the bean.agentId.get
        .build()),
      filtersOpt)

  private def createAgentId(agentId: String): AgentID =
    AgentID.newBuilder
      .setValue(agentId)
      .build

  private def createOfferId(offerId: String): OfferID =
    OfferID.newBuilder
      .setValue(offerId)
      .build

  override def accept(offerIds: Iterable[Protos.OfferID], offerOperations: List[Offer.Operation], filtersOpt: Option[Protos.Filters] = None): Unit =
    sendCall(Call.newBuilder().setAccept(
      filtersOpt match {
        case Some(filters) => Accept.newBuilder.addAllOfferIds(offerIds.asJava).addAllOperations(offerOperations.asJava).setFilters(filters)
        case _ => Accept.newBuilder.addAllOfferIds(offerIds.asJava).addAllOperations(offerOperations.asJava)
      }
    ), ACCEPT)

  override def teardown(): Unit = sendCall(Call.newBuilder(), TEARDOWN)

  private def sendCall(callBuilder: Call.Builder, callType: Call.Type): Unit =
    sendCall(callBuilder.setType(callType)
      .setFrameworkId(frameworkID)
      .build)

  private def sendCall(call: Call): Unit = {
    if (publishSubject == null) {
      throw new RuntimeException("No publisher found, please call subscribe before sending anything.")
    }
    publishSubject.onNext(Optional.of(SinkOperations.create(call)))
  }

  override def decline(offerIds: Iterable[Protos.OfferID], filtersOpt: Option[Protos.Filters] = None): Unit =
    sendCall(Call.newBuilder().setDecline(
      filtersOpt match {
        case Some(filters) => Decline.newBuilder.addAllOfferIds(offerIds.asJava).setFilters(filters)
        case _ => Decline.newBuilder.addAllOfferIds(offerIds.asJava)
      }
    ), DECLINE)

  override def acceptInverseOffers(offerIds: List[Protos.OfferID], filtersOpt: Option[Protos.Filters] = None): Unit =
    sendCall(Call.newBuilder().setAcceptInverseOffers(
      filtersOpt match {
        case Some(filters) => AcceptInverseOffers.newBuilder.addAllInverseOfferIds(offerIds.asJava).setFilters(filters)
        case _ => AcceptInverseOffers.newBuilder.addAllInverseOfferIds(offerIds.asJava)
      }), ACCEPT_INVERSE_OFFERS)

  override def declineInverseOffers(offerIds: List[Protos.OfferID], filtersOpt: Option[Protos.Filters] = None): Unit =
    sendCall(Call.newBuilder().setDeclineInverseOffers(
      filtersOpt match {
        case Some(filters) => DeclineInverseOffers.newBuilder.addAllInverseOfferIds(offerIds.asJava).setFilters(filters)
        case _ => DeclineInverseOffers.newBuilder.addAllInverseOfferIds(offerIds.asJava)
      }), DECLINE_INVERSE_OFFERS)

  override def kill(taskId: Protos.TaskID, agentIdOpt: Option[Protos.AgentID] = None, killPolicyOpt: Option[Protos.KillPolicy] = None): Unit =
    sendCall(Call.newBuilder().setKill({
      (agentIdOpt, killPolicyOpt) match {
        case (Some(agentId), Some(killPolicy)) => Kill.newBuilder.setTaskId(taskId).setAgentId(agentId).setKillPolicy(killPolicy)
        case (Some(agentId), None) => Kill.newBuilder.setTaskId(taskId).setAgentId(agentId)
        case (None, Some(killPolicy)) => Kill.newBuilder.setTaskId(taskId).setKillPolicy(killPolicy)
        case _ => Kill.newBuilder.setTaskId(taskId)
      }
    }), KILL)

  override def revive(): Unit = sendCall(Call.newBuilder(), REVIVE)

  override def shutdown(executorId: Protos.ExecutorID, agentIdOpt: Option[Protos.AgentID] = None): Unit =
    sendCall(Call.newBuilder().setShutdown({
      agentIdOpt match {
        case Some(agentId) => Shutdown.newBuilder().setExecutorId(executorId).setAgentId(agentId)
        case _ => Shutdown.newBuilder().setExecutorId(executorId)
      }
    }), SHUTDOWN)

  override def reconsile(tasks: Iterable[Reconcile.Task]): Unit =
    sendCall(Call.newBuilder()
      .setReconcile(Reconcile.newBuilder
        .addAllTasks(tasks.asJava)), RECONCILE)

  override def message(agentId: Protos.AgentID, executorId: Protos.ExecutorID, data: ByteString): Unit =
    sendCall(Call.newBuilder()
      .setMessage(Message.newBuilder
        .setAgentId(agentId)
        .setExecutorId(executorId)
        .setData(data)), MESSAGE)

  override def request(requests: List[Protos.Request]): Unit =
    sendCall(Call.newBuilder()
      .setRequest(Request.newBuilder
        .addAllRequests(requests.asJava)), REQUEST)

  override def close(): Unit = {
    if (openStream != null && !openStream.isUnsubscribed) {
      openStream.unsubscribe()
    }
  }
}
