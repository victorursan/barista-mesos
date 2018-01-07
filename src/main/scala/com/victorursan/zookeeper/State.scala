package com.victorursan.zookeeper

import com.victorursan.state.{AgentResources, Bean, BeanDocker, Offer}


/**
  * Created by victor on 4/24/17.
  */
trait State {
  def getNextId: String

  def getOverview: Map[String, String]

  def addToOverview(taskId: String, state: String): Map[String, String]

  def removeFromOverview(taskId: String): Map[String, String]

  def awaitingBeans: Set[Bean]

  def tasksToKill: Set[String]

  def addToAccept(bean: Bean): Set[Bean]

  def addToAccept(beans: Set[Bean]): Set[Bean]

  def removeFromAccept(bean: Bean): Set[Bean]

  def removeFromAccept(beans: Set[Bean]): Set[Bean]

  def addToRunningUnpacked(bean: Bean): Set[Bean]

  def addToOldBeans(bean: Bean): Set[Bean]

  def addToOldBeans(beans: Set[Bean]): Set[Bean]

  def oldBeans: Set[Bean]

  def removeOldBean(bean: Bean): Set[Bean]

  def removeOldBean(beans: Set[Bean]): Set[Bean]

  def addToRunningUnpacked(beans: Set[Bean]): Set[Bean]

  def runningUnpacked: Set[Bean]

  def removeRunningUnpacked(bean: Bean): Set[Bean]

  def removeRunningUnpacked(beans: Set[Bean]): Set[Bean]

  def addToKill(taskID: String): Set[String]

  def addToKill(tasksID: Set[String]): Set[String]

  def removeFromKill(taskID: String): Set[String]

  def removeFromKill(taskIDs: Set[String]): Set[String]

  def addToOffer(offer: Offer): Set[Offer]

  def addToOffer(offers: Set[Offer]): Set[Offer]

  def removeFromOffer(offer: String): Set[Offer]

  def removeFromOffer(offers: Set[String]): Set[Offer]

  def addToBeanDocker(beanDocker: BeanDocker): Set[BeanDocker]

  def addToBeanDocker(beanDockers: Set[BeanDocker]): Set[BeanDocker]

  def removeFromBeanDocker(taskId: String): Set[BeanDocker]

  def removeFromBeanDocker(tasksId: Set[String]): Set[BeanDocker]

  def availableBeanDocker: Set[BeanDocker]

  def availableOffers: Set[Offer]

  def agentResources: Map[String, AgentResources]

  def updateAgentResources(agentResources: Map[String, AgentResources]): Map[String, AgentResources]

}
