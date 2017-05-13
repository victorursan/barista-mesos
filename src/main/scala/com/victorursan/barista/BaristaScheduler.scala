package com.victorursan.barista

import com.victorursan.state.{Bean, DockerPort, ScheduleState}
import org.apache.mesos.v1.Protos.Offer

import scala.collection.JavaConverters._

/**
  * Created by victor on 5/3/17.
  */
object BaristaScheduler {

  def scheduleBeans(beans: Set[Bean], offers: List[Offer]): ScheduleState = {
    var remainingOffers = offers
    var acceptOffers = Set[(Bean, String)]()
    var scheduledBeans = Set[Bean]()
    for (bean <- beans) {
      scheduleBean(bean, remainingOffers).foreach { case (portBean, offer) =>
        remainingOffers = remainingOffers.filterNot(_.equals(offer))
        scheduledBeans = scheduledBeans + bean
        acceptOffers = acceptOffers + (portBean.copy(agentId = Some(offer.getAgentId.getValue), hostname = Some(offer.getUrl.getAddress.getHostname)) -> offer.getId.getValue)
      }
    }
    ScheduleState(acceptOffers, remainingOffers, scheduledBeans)
  }

  private def scheduleBean(bean: Bean, offers: List[Offer]): Option[(Bean, Offer)] = {
    offers.map { offer => (beanWithHostPort(bean, offer), offer) }
      .flatMap {
        case (Some(bean: Bean), offer: Offer) => Option((bean, offer))
        case _ => None
      }
      .find { case (bean: Bean, offer: Offer) =>
        val offerPorts = portsFromOffer(offer)
        memFromOffer(offer) >= bean.dockerEntity.resource.mem &&
          cpusFromOffer(offer) >= bean.dockerEntity.resource.cpu &&
          bean.dockerEntity.resource.ports.forall(dockerPort => offerPorts.exists(range => dockerPort.hostPort.exists(range.contains)))
      }
  }

  private def memFromOffer(offer: Offer): Double =
    offer.getResourcesList.asScala.find(_.getName.equalsIgnoreCase("mem")).map(_.getScalar.getValue).getOrElse(0)

  private def cpusFromOffer(offer: Offer): Double =
    offer.getResourcesList.asScala.find(_.getName.equalsIgnoreCase("cpus")).map(_.getScalar.getValue).getOrElse(0)

  private def beanWithHostPort(bean: Bean, offer: Offer): Option[Bean] = {
    val hostPorts = portsFromOffer(offer).flatten
    val oldBeanPorts = bean.dockerEntity.resource.ports.groupBy(_.hostPort.isDefined)
    val portsToBeAssign = oldBeanPorts.getOrElse(false, List())
    val assignedPorts = hostPorts.take(portsToBeAssign.length)
      .zip(portsToBeAssign)
      .map { case (hostPort: Int, dockerPort: DockerPort) => dockerPort.copy(hostPort = Some(hostPort)) }
      .toList
    if (assignedPorts.length == portsToBeAssign.length) {
      Some(bean.copy(dockerEntity =
        bean.dockerEntity.copy(resource =
          bean.dockerEntity.resource.copy(ports =
            assignedPorts ++ oldBeanPorts.getOrElse(true, List())))))
    } else {
      None
    }
  }

  private def portsFromOffer(offer: Offer): Stream[Range.Inclusive] =
    offer.getResourcesList.asScala
      .find(_.getName.equalsIgnoreCase("ports"))
      .map(_.getRanges.getRangeList.asScala.map(range => range.getBegin.toInt to range.getEnd.toInt).toStream)
      .getOrElse(Stream.empty)

}
