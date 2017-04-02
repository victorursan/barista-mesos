package com.victorursan.barista

import org.slf4j.LoggerFactory

/**
  * Created by victor on 4/2/17.
  */
class BaristaController {
    private val log = LoggerFactory.getLogger(classOf[BaristaController])
  //  private val scheduler = new BaristaScheduler
  //  private val mesosMaster = System.getenv("ZK")
  //  private val driver = BaristaSchedulerDriver.newDriver(scheduler, mesosMaster)
  //
  //  private val resources: mutable.Map[String, Offer] = mutable.Map()
  //
  //  //  scheduler.subscribe(new Subscriber[List[Offer]]() {
  //  //    log.info(s"${classOf[BaristaController]} subscribed to scheduler: ${scheduler.getClass}")
  //  //    override def onNext(list: List[Offer]): Unit = list.foreach(offer => resources(offer.getSlaveId.getValue) = offer)
  //  //    override def onError(error: Throwable): Unit = log.error(s"subscriber: ${error.printStackTrace()}")
  //  //    override def onCompleted(): Unit = log.error(s"subscriber completed")
  //  //  })
  //
  //  def start(): Unit = {
  //    log.info(s"driver start")
  //    driver.start()
  //  }
  //
  //  def stop(): Unit = {
  //    log.info(s"driver stop")
  //    driver.stop()
  //  }
  //
  //  def offers(): List[Offer] = resources.values.toList
  //
  //  def launchDockerEntity(dockerEntity: DockerEntity): Status = {
  //    log.info(s"launchDockerEntity: dockerEntity: $dockerEntity")
  //    val offer: Offer = BaristaSchedulerHelper.bestOfferForEntity(resources.values.toList, dockerEntity)
  //    val task: TaskInfo = TaskHandler.createTaskWith(offer, dockerEntity)
  //    launchTaskWithOffer(offer, task)
  //  }
  //
  //  private def launchTaskWithOffer(offer: Offer, taskInfo: TaskInfo): Status = {
  //    log.info(s"launchTaskWithOffer: offer: $offer, taskInfo: $taskInfo")
  //    driver.launchTasks(List(offer.getId).asJavaCollection, List(taskInfo).asJavaCollection)
  //  }
  //
}
