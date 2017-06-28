package com.victorursan.barista

import java.lang.Math.min

import com.victorursan.state.{Bean, Offer, ScheduleState}
import com.victorursan.zookeeper.StateController

object RoundRobinScheduler extends Scheduler{

  override def schedule(beans: Set[Bean], offers: List[Offer]): ScheduleState = {
    var nextOfferIndex = StateController.roundRobinIndex

    val agents: List[String] = StateController.agentResources.keySet.toList.sortBy(_.hashCode)

    val remainingOffers = offers.groupBy(_.agentId)
    var remainingBeans = beans

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


}
