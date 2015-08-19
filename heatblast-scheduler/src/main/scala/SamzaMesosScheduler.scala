package com.banno.heatblast

import org.apache.mesos.{Scheduler, SchedulerDriver}
import org.apache.mesos.Protos._
import java.util.{List => JavaList}
import scala.collection.JavaConverters._
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

trait SamzaMesosScheduler extends Scheduler with SamzaJobStatePersistence with Logging {

  private[this] val infosToCompute: BlockingQueue[RunSamzaJob] = new LinkedBlockingQueue(1000)
  def computeJobInfo(command: RunSamzaJob): Boolean = infosToCompute.offer(command) //TODO this command queue needs to be part of scheduler's persistent state

  def getOfferForComputingJobInfo(command: RunSamzaJob, offers: Seq[Offer]): Option[Offer] = ???

  def useOffersForComputingJobInfo(offers: Seq[Offer]): Seq[Offer] = {
    if (!infosToCompute.isEmpty) {
      val command = infosToCompute.peek
      getOfferForComputingJobInfo(command, offers) match {
        case Some(offer) => ???
        case None => ???
      }
    } else {
      offers
    }
  }

  override def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo): Unit = {
    log.info(s"Registered frameworkdId $frameworkId")
  }

  override def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo): Unit = {
    log.info(s"Reregistered masterInfo $masterInfo")
  }

  override def resourceOffers(driver: SchedulerDriver, joffers: JavaList[Offer]): Unit = {
    val offers = joffers.asScala
    log.info(s"Received offers: $offers")
    val unusedOffers = useOffersForComputingJobInfo(offers)
    unusedOffers.foreach(o => driver.declineOffer(o.getId))
  }

  override def offerRescinded(driver: SchedulerDriver, offerId: OfferID): Unit = {
    log.info(s"Offer rescinded $offerId")
  }

  override def statusUpdate(driver: SchedulerDriver, status: TaskStatus): Unit = {
    log.info(s"Status update $status")
  }

  override def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]): Unit = {
    log.info("Framework message")
  }

  override def disconnected(driver: SchedulerDriver): Unit = {
    log.info("Disconnected")
  }

  override def slaveLost(driver: SchedulerDriver, slaveId: SlaveID): Unit = {
    log.info(s"Slave lost $slaveId")
  }

  override def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int): Unit = {
    log.info(s"Executor lost $executorId")
  }

  override def error(driver: SchedulerDriver, message: String): Unit = {
    log.info(s"Error $message")
  }
}
