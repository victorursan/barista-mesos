package com.victorursan.barista.scheduler

import com.victorursan.state.{Bean, DockerPort, Offer, ScheduleState}
import com.victorursan.utils.MesosConf

trait Scheduler extends MesosConf {
  def schedule(beans: Set[Bean], offers: List[Offer]): ScheduleState

  protected def resolveBeanWithHost(bean: Bean, mesosOffer: Offer): Option[Bean] = {
    val beanPResources = bean.dockerEntity.resource

    if (beanPResources.cpu <= mesosOffer.cpu && beanPResources.mem <= mesosOffer.mem) {
      beanWithHostPort(bean, mesosOffer)
    } else {
      None
    }
  }

  private def beanWithHostPort(bean: Bean, mesosOffer: Offer): Option[Bean] = {
    val hostPorts = mesosOffer.ports.toStream.flatten
    val oldBeanPorts = bean.dockerEntity.resource.ports.groupBy(_.hostPort.isDefined)
    val portsToBeAssign = oldBeanPorts.getOrElse(false, List())
    val assignedPorts = hostPorts.take(portsToBeAssign.length).toList
      .zip(portsToBeAssign)
      .map { case (hostPort: Int, dockerPort: DockerPort) => dockerPort.copy(hostPort = Some(hostPort)) }
    if (assignedPorts.lengthCompare(portsToBeAssign.size) == 0) {
      Some(bean.copy(dockerEntity =
        bean.dockerEntity.copy(resource =
          bean.dockerEntity.resource.copy(ports =
            assignedPorts ++ oldBeanPorts.getOrElse(true, List()))), agentId = Some(mesosOffer.agentId), hostname = Some(mesosOffer.hostname)))
    } else {
      None
    }
  }
}
