package com.victorursan.barista

import com.victorursan.state.{AgentResources, Bean, Offer, ScheduleState}
import com.victorursan.zookeeper.StateController

object HostCompressionScheduler extends Scheduler {
  override def schedule(beans: Set[Bean], offers: List[Offer]): ScheduleState = {
    StateController.agentResources

    var acceptOffers = Set[(Bean, String)]()
    var scheduledBeans = Set[String]()


    val agents: List[String] = StateController.agentResources.toList.sortBy { case (agent: String, resources: AgentResources) =>
      if (schedulerResource == "mem") resources.mem else resources.cpus
    }.map(_._1).reverse

    var remainingOffers: List[Offer] = offers.sortBy(offer => (agents.indexOf(offer.agentId), if (schedulerResource == "mem") offer.mem else offer.cpu))

    val sortedBeans: List[Bean] = beans.toList.sortBy(bean =>
      if (schedulerResource == "mem") bean.dockerEntity.resource.mem else bean.dockerEntity.resource.cpu
      ).reverse

    sortedBeans.headOption.foreach(bean =>
      remainingOffers.find(offer => {
        resolveBeanWithHost(bean, offer).exists(beanp => {
          remainingOffers = remainingOffers.filterNot(_.equals(offer))
          acceptOffers = acceptOffers + (beanp -> offer.id) //the offer is accepted
          scheduledBeans = scheduledBeans + beanp.taskId
          true
        })
      })
    )



    ScheduleState(acceptOffers, remainingOffers, scheduledBeans)
  }

  //  private def beanWithHostPort(bean: Bean, mesosOffer: Offer): Option[Bean] = {
  //    val hostPorts = mesosOffer.ports.toStream.flatten
  //    val oldBeanPorts = bean.dockerEntity.resource.ports.groupBy(_.hostPort.isDefined)
  //    val portsToBeAssign = oldBeanPorts.getOrElse(false, List())
  //    val assignedPorts = hostPorts.take(portsToBeAssign.length).toList
  //      .zip(portsToBeAssign)
  //      .map { case (hostPort: Int, dockerPort: DockerPort) => dockerPort.copy(hostPort = Some(hostPort)) }
  //    if (assignedPorts.length == portsToBeAssign.length) {
  //      Some(bean.copy(dockerEntity =
  //        bean.dockerEntity.copy(resource =
  //          bean.dockerEntity.resource.copy(ports =
  //            assignedPorts ++ oldBeanPorts.getOrElse(true, List())))))
  //    } else {
  //      None
  //    }
  //  }
}
