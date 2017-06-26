package com.victorursan.barista

import com.victorursan.state.{Bean, DockerPort, Offer, ScheduleState}
import com.victorursan.zookeeper.StateController

object RoundRobinScheduler extends Scheduler {
  override def schedule(beans: Set[Bean], offers: List[Offer]): ScheduleState = {
    StateController.agentResources
    var remainingOffers = offers
    var acceptOffers = Set[(Bean, String)]()
    var scheduledBeans = Set[Bean]()
    for (bean <- beans) {
      scheduleBean(bean, remainingOffers).foreach { case (portBean, offer) =>
        remainingOffers = remainingOffers.filterNot(_.equals(offer))
        scheduledBeans = scheduledBeans + bean
        acceptOffers = acceptOffers + (portBean.copy(agentId = Some(offer.agentId), hostname = Some(offer.hostname)) -> offer.id)
      }
    }
    ScheduleState(acceptOffers, remainingOffers, scheduledBeans)
  }

  private def scheduleBean(bean: Bean, offers: List[Offer]): Option[(Bean, Offer)] = {
    offers.sortBy(_.mem).reverse.map { offer => (beanWithHostPort(bean, offer), offer) }
      .flatMap {
        case (Some(bean: Bean), offer: Offer) => Option((bean, offer))
        case _ => None
      }
      .find { case (bean: Bean, offer: Offer) =>
        offer.mem >= bean.dockerEntity.resource.mem &&
          offer.cpu >= bean.dockerEntity.resource.cpu &&
          bean.dockerEntity.resource.ports.forall(dockerPort => offer.ports.exists(range => dockerPort.hostPort.exists(range.contains)))
      }
  }

  private def beanWithHostPort(bean: Bean, mesosOffer: Offer): Option[Bean] = {
    val hostPorts = mesosOffer.ports.toStream.flatten
    val oldBeanPorts = bean.dockerEntity.resource.ports.groupBy(_.hostPort.isDefined)
    val portsToBeAssign = oldBeanPorts.getOrElse(false, List())
    val assignedPorts = hostPorts.take(portsToBeAssign.length).toList
      .zip(portsToBeAssign)
      .map { case (hostPort: Int, dockerPort: DockerPort) => dockerPort.copy(hostPort = Some(hostPort)) }
    if (assignedPorts.length == portsToBeAssign.length) {
      Some(bean.copy(dockerEntity =
        bean.dockerEntity.copy(resource =
          bean.dockerEntity.resource.copy(ports =
            assignedPorts ++ oldBeanPorts.getOrElse(true, List())))))
    } else {
      None
    }
  }
}
