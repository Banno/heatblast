package com.banno.heatblast

import org.apache.mesos.{Scheduler, SchedulerDriver}
import org.apache.mesos.Protos._
import java.util.{List => JavaList}
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._

abstract class SamzaMesosScheduler extends Scheduler with SamzaJobStatePersistence {

  private[this] lazy val log = LoggerFactory.getLogger(this.getClass)

  def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo): Unit = {
    log.info(s"Registered frameworkdId $frameworkId")
  }

  def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo): Unit = {
    log.info(s"Reregistered masterInfo $masterInfo")
  }

  def resourceOffers(driver: SchedulerDriver, joffers: JavaList[Offer]): Unit = {
    val offers = joffers.asScala
    log.info(s"Received offers: $offers")
  }

  def offerRescinded(driver: SchedulerDriver, offerId: OfferID): Unit = {
    log.info(s"Offer rescinded $offerId")
  }

  def statusUpdate(driver: SchedulerDriver, status: TaskStatus): Unit = {
    log.info(s"Status update $status")
  }

  def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]): Unit = {
    log.info("Framework message")
  }

  def disconnected(driver: SchedulerDriver): Unit = {
    log.info("Disconnected")
  }

  def slaveLost(driver: SchedulerDriver, slaveId: SlaveID): Unit = {
    log.info(s"Slave lost $slaveId")
  }

  def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int): Unit = {
    log.info(s"Executor lost $executorId")
  }

  def error(driver: SchedulerDriver, message: String): Unit = {
    log.info(s"Error $message")
  }
}
