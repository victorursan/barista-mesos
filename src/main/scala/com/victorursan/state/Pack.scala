package com.victorursan.state

/**
  * Created by victor on 5/5/17.
  */

case class QuantityBean(quantity: Int, bean: RawBean, taskIds: Option[Set[String]] = Some(Set()))

case class Pack(name: String, mix: Set[QuantityBean], autoScaling: AutoScaling)

case class AutoScaling(algorithm: String, resource: String, thresholds: Thresholds)

case class Thresholds(load: List[Double], time: List[Double], cooldown: List[Double], boundaries: List[Int])
