package com.victorursan.barista

import java.lang.Math.min

import com.victorursan.state.{Bean, Offer, ScheduleState}
import com.victorursan.utils.MesosConf
import com.victorursan.zookeeper.StateController

object RoundRobinScheduler extends Scheduler with MesosConf {

  override def schedule(beans: Set[Bean], offers: List[Offer]): ScheduleState = {
    val roundRobinIndex = StateController.roundRobinIndex

    val agents: List[String] = StateController.agentResources.keySet.toList.sortBy(_.hashCode)

    val remainingOffers = offers.groupBy(_.agentId)
    var remainingBeans = beans

    var nextOfferIndex = roundRobinIndex
    //    var nextOfferIndex = remainingOffers.zipWithIndex.takeWhile {
    //      case (o: Offer, i: Int) => {
    //        o.agentId.hashCode < nextAgent.hashCode
    //      }
    //    }.last._2 + 1

    var acceptOffers = Set[(Bean, String)]()
    var scheduledBeans = Set[String]()


    (1 to min(agents.size, beans.size)) foreach { _ =>
      remainingOffers.get(agents(nextOfferIndex % agents.size)).foreach(offers => {
        val topOffer: Option[Offer] = offers.sortBy(offer =>
          if (schedulerResource == "mem") {
            offer.mem
          } else {
            offer.cpu
          }).reverse.headOption
        topOffer.foreach(offerToSchedule => {
          val scheduleBean: Option[Bean] = remainingBeans.flatMap(bean => resolveBeanWithHost(bean, offerToSchedule)).headOption
          scheduleBean.foreach(bean => {
            remainingBeans = remainingBeans.filterNot(_.taskId == bean.taskId)
            scheduledBeans = scheduledBeans + bean.taskId
            acceptOffers = acceptOffers + (bean -> offerToSchedule.id)
          })
        })
      })


      nextOfferIndex = StateController.incRoundRobinIndex
    }

    ScheduleState(acceptOffers, remainingOffers.values.flatten.filterNot(off => acceptOffers.map(_._2).contains(off.id)), scheduledBeans)
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


}
