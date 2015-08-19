package com.banno.heatblast

import org.apache.mesos.{Scheduler, SchedulerDriver}
import org.apache.mesos.Protos._
import java.util.{List => JavaList}

class SamzaMesosScheduler extends Scheduler with SamzaJobStatePersistence {

  def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo): Unit = {}

  def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo): Unit = {}

  def resourceOffers(driver: SchedulerDriver, offers: JavaList[Offer]): Unit = {}

  def offerRescinded(driver: SchedulerDriver, offerId: OfferID): Unit = {}

  def statusUpdate(driver: SchedulerDriver, status: TaskStatus): Unit = {}

  def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]): Unit = {}

  def disconnected(driver: SchedulerDriver): Unit = {}

  def slaveLost(driver: SchedulerDriver, slaveId: SlaveID): Unit = {}

  def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int): Unit = {}

  def error(driver: SchedulerDriver, message: String): Unit = {}
}
