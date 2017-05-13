package com.victorursan.state

import org.apache.mesos.v1.Protos.Offer

/**
  * Created by victor on 5/3/17.
  */
case class ScheduleState(scheduledBeans: Set[(Bean, String)], canceledOffers: Iterable[Offer], consumedBeans: Set[Bean])
