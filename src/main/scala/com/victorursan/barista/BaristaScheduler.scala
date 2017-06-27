package com.victorursan.barista

import com.victorursan.state.{Bean, DockerPort, Offer, ScheduleState}


/**
  * Created by victor on 5/3/17.
  */
object BaristaScheduler extends Scheduler {
  override def schedule(beans: Set[Bean], offers: List[Offer]): ScheduleState = {
    var remainingOffers = offers
    var acceptOffers = Set[(Bean, String)]()
    var scheduledBeans = Set[String]()
    for (bean <- beans) {
      scheduleBean(bean, remainingOffers).foreach { case (portBean, offer) =>
        remainingOffers = remainingOffers.filterNot(_.equals(offer))
        scheduledBeans = scheduledBeans + bean.taskId
        acceptOffers = acceptOffers + (portBean -> offer.id)
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

}
