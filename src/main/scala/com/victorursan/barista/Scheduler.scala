package com.victorursan.barista

import com.victorursan.state.{Bean, Offer, ScheduleState}

trait Scheduler {
  def schedule(beans: Set[Bean], offers: List[Offer]): ScheduleState
}
