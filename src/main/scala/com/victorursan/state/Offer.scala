package com.victorursan.state

/**
  * Created by victor on 5/26/17.
  */
case class Offer(id: String, agentId: String, hostname: String, mem: Double, cpu: Double, ports: List[Range.Inclusive])
