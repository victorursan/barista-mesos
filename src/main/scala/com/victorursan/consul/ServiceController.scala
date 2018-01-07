package com.victorursan.consul

import java.util.Collections

import com.ecwid.consul.v1.agent.model.NewService
import com.ecwid.consul.v1.health.model.Check
import com.ecwid.consul.v1.{ConsulClient, QueryParams}

import scala.collection.JavaConverters._

/**
  * Created by victor on 5/11/17.
  */
object ServiceController {

  def registerService(consulClient: ConsulClient, baristaService: BaristaService): Unit = {
    val service = new NewService()
    service.setId(baristaService.id)
    service.setName(baristaService.name)
    service.setTags(baristaService.tags.asJava)
    service.setChecks(baristaService.checks.map(baristaCheckToCheck).asJava)
    service.setAddress(baristaService.serviceAddress)
    service.setPort(baristaService.servicePort)

    consulClient.agentServiceRegister(service)
  }

  def setLoadBalancer(consulClientStr: String, loadBalancing: String): Unit = {
    val consulClient = new ConsulClient(consulClientStr)
    //    consulClient.setKVValue("/balancer", loadBalancing);
  }

  def deregisterService(consulClient: ConsulClient, serviceId: String): Unit =
    consulClient.agentServiceDeregister(serviceId)

  def registerService(consulClientStr: String, baristaService: BaristaService): Unit = {
    val service = new NewService()
    service.setId(baristaService.id)
    service.setName(baristaService.name)
    service.setTags(baristaService.tags.asJava)
    service.setChecks(baristaService.checks.map(baristaCheckToCheck).asJava)
    service.setAddress(baristaService.serviceAddress)
    service.setPort(baristaService.servicePort)

    val consulClient = new ConsulClient(consulClientStr)
    consulClient.agentServiceRegister(service)
  }

  private def baristaCheckToCheck(baristaCheck: BaristaCheck): NewService.Check = {
    val check = new NewService.Check
    check.setHttp(baristaCheck.httpHealth.toString)
    check.setInterval(s"${baristaCheck.interval.toString}s")
    check
  }

  def deregisterService(consulClientStr: String, serviceId: String): Unit = {
    val consulClient = new ConsulClient(consulClientStr)
    consulClient.agentServiceDeregister(serviceId)
  }

  //todo if serviceName = "" it throws exception, should be catched a layer up
  def serviceHealthChecks(consulClient: ConsulClient, serviceName: String, queryParams: QueryParams = QueryParams.DEFAULT): Iterable[Check] =
    Option(consulClient.getHealthChecksForService(serviceName, queryParams).getValue)
      .getOrElse(Collections.emptyList[Check]())
      .asScala

  private def setLoadBalancer(consulClient: ConsulClient, loadBalancing: String): Unit = {
    //    consulClient.setKVValue("/balancer", loadBalancing);
  }

}
