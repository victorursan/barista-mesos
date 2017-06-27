package com.victorursan.state

/**
  * Created by victor on 5/3/17.
  */
case class ScheduleState(scheduledBeans: Set[(Bean, String)], canceledOffers: Iterable[Offer], consumedBeans: Set[String])
