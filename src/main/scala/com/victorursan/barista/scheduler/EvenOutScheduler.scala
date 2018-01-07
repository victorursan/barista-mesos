package com.victorursan.barista.scheduler

import com.victorursan.state.{Bean, Offer, ScheduleState}
import com.victorursan.zookeeper.StateController

object EvenOutScheduler extends Scheduler {
  override def schedule(beans: Set[Bean], offers: List[Offer]): ScheduleState = {
    var remainingOffers = offers
    var remainingOffersMap: Map[String, List[Offer]] = remainingOffers.groupBy(_.agentId).withDefaultValue(List())
    var runningBeans: Map[String, Set[Bean]] = StateController.runningUnpacked.groupBy(_.agentId.get)
    runningBeans = runningBeans ++ remainingOffersMap.keySet.diff(runningBeans.keySet).map(_ -> Set[Bean]()).toMap

    var acceptOffers = Set[(Bean, String)]()
    var scheduledBeans = Set[String]()

    for (bean <- beans) {
      getFreeAgents(runningBeans, bean).toList.sortBy(_._1).find { //a list with agents with  least number of services of type "bean" on them
        case (frequncy: Int, agents: Set[String]) =>
          agents.exists(agent => { //todo sort agents by the most resoruces
            val offers = remainingOffersMap(agent).sortBy(offer => //get any offers on them agents sorted by who has the most resources
              if (schedulerResource == "mem") {
                offer.mem
              } else {
                offer.cpu
              }).reverse

            if (offers.nonEmpty) {
              offers.exists(off => {
                resolveBeanWithHost(bean, off).exists(bbean => {
                  //find  the first offer good enough
                  scheduledBeans = scheduledBeans + bbean.taskId //add the bean to be scheduled
                  acceptOffers = acceptOffers + (bbean -> off.id) //the offer is accepted
                  remainingOffers = remainingOffers.filterNot(_.id == off.id)
                  remainingOffersMap = remainingOffers.groupBy(_.agentId).withDefaultValue(List()) //remove the offer from available
                  runningBeans = runningBeans + (agent -> (runningBeans(agent) + bbean)) // add the bean as a running service
                  true
                })
              })
            } else {
              false
            }
          })
      }
    }
    ScheduleState(acceptOffers, remainingOffers, scheduledBeans)
  }


  private def getFreeAgents(runningBeans: Map[String, Set[Bean]], bean: Bean): Map[Int, Set[String]] = {

    val frequencyMap =
      runningBeans.groupBy { case (agent: String, beans: Set[Bean]) => beans.count(b => b.pack == bean.pack && b.name == bean.name) }
        .map { case (count: Int, map: Map[String, Set[Bean]]) => count -> map.keySet }
    frequencyMap
    //    frequencyMap.getOrElse(frequencyMap.keySet.min, Set())
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
