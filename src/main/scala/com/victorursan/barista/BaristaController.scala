package com.victorursan.barista

import com.victorursan.utils.{ BaristaSchedulerHelper, DockerEntity, TaskHandler }
import org.apache.mesos.Protos._
import rx.lang.scala.Subscriber

import scala.collection.JavaConverters._
import scala.collection._

/**
 * Created by victor on 4/25/16.
 */
class BaristaController {

  private val scheduler = new BaristaScheduler
  private val mesosMaster = System.getenv("ZK")
  private val driver = BaristaSchedulerDriver.newDriver(scheduler, mesosMaster)

  private val resources: mutable.Map[String, Offer] = mutable.Map()

  scheduler.subscribe(new Subscriber[List[Offer]]() {
    override def onNext(list: List[Offer]): Unit = list.foreach(offer => resources(offer.getSlaveId.getValue) = offer)
    override def onError(error: Throwable): Unit = error.printStackTrace()
    override def onCompleted(): Unit = {}
  })

  def start(): Unit = driver.start()

  def stop(): Unit = driver.stop()

  def offers(): List[Offer] = resources.values.toList

  def launchDockerEntity(dockerEntity: DockerEntity): Status = {
    val offer: Offer = BaristaSchedulerHelper.bestOfferForEntity(resources.values.toList, dockerEntity)
    val task: TaskInfo = TaskHandler.createTaskWith(offer, dockerEntity)
    launchTaskWithOffer(offer, task)
  }

  private def launchTaskWithOffer(offer: Offer, taskInfo: TaskInfo): Status =
    driver.launchTasks(List(offer.getId).asJavaCollection, List(taskInfo).asJavaCollection)

}
