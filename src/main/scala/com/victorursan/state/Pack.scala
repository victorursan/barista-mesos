package com.victorursan.state

/**
  * Created by victor on 5/5/17.
  */

case class QuantityBean(quantity: Int, bean: RawBean, taskIds: Option[Set[String]] = Some(Set()))

case class Pack(name: String, mix: Set[QuantityBean])
