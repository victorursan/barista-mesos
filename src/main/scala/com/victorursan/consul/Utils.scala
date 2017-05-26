package com.victorursan.consul

import java.net.URL

import com.victorursan.state.{Bean, DockerPort, Offer}
import org.apache.mesos.v1.Protos

import scala.collection.JavaConverters._

/**
  * Created by victor on 5/13/17.
  */
object Utils {

  def convertBeanToService(bean: Bean, servicePort: Int): BaristaService =
    BaristaService(bean.taskId, bean.name, serviceAddress = bean.hostname.get, servicePort = servicePort,
      checks = bean.checks.map(bc => BaristaCheck(new URL(s"http://${bean.hostname.get}:$servicePort${bc.httpPath}"), bc.interval)))

  def convertOffers(mesosOffers: Iterable[Protos.Offer]): Iterable[Offer] = mesosOffers.map(convertOffer)


  def convertOffer(mesosOffer: Protos.Offer): Offer = {
    val id = mesosOffer.getId.getValue
    val agentId = mesosOffer.getAgentId.getValue
    val hostname = mesosOffer.getUrl.getAddress.getHostname
    val mem = memFromOffer(mesosOffer)
    val cpu = cpusFromOffer(mesosOffer)
    val ports = portsFromOffer(mesosOffer)

    Offer(id=id, agentId = agentId, hostname = hostname, mem = mem, cpu = cpu, ports = ports)
  }

  private def memFromOffer(mesosOffer: Protos.Offer): Double =
    mesosOffer.getResourcesList.asScala.find(_.getName.equalsIgnoreCase("mem")).map(_.getScalar.getValue).getOrElse(0)

  private def cpusFromOffer(mesosOffer: Protos.Offer): Double =
    mesosOffer.getResourcesList.asScala.find(_.getName.equalsIgnoreCase("cpus")).map(_.getScalar.getValue).getOrElse(0)


  private def portsFromOffer(mesosOffer: Protos.Offer): List[Range.Inclusive] =
    mesosOffer.getResourcesList.asScala
      .find(_.getName.equalsIgnoreCase("ports"))
      .map(_.getRanges.getRangeList.asScala.map(range => range.getBegin.toInt to range.getEnd.toInt).toStream)
      .getOrElse(Stream.empty)
      .toList

}
