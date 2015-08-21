package com.banno.heatblast

import org.apache.mesos.{Scheduler, SchedulerDriver}
import org.apache.mesos.Protos._
import java.util.{List => JavaList}
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}
import java.util.UUID
import org.apache.samza.config.{Config => SamzaConfig}
import org.apache.samza.job.{CommandBuilder, ShellCommandBuilder}
import org.apache.samza.container.{TaskName, TaskNamesToSystemStreamPartitions}

case class Resources(cpus: Double, memory: Double)
object Resources {
  def fromOffer(offer: Offer): Resources = {
    val resourceMap = offer.getResourcesList.map(r => (r.getName, r.getScalar.getValue)).toMap
    Resources(resourceMap("cpus"), resourceMap("mem"))
  }
}

trait SamzaMesosScheduler extends Scheduler with SamzaJobStatePersistence with Logging with HeatblastConfig {

  private[this] val infosToCompute: BlockingQueue[RunSamzaJob] = new LinkedBlockingQueue(1000)
  def computeJobInfo(command: RunSamzaJob): Boolean = infosToCompute.offer(command) //TODO this command queue needs to be part of scheduler's persistent state

  //TODO get these resource reqs from config
  val infoCpus = 1d
  val infoMemory = 100d

  def getOfferForComputingJobInfo(command: RunSamzaJob, offers: Seq[Offer]): Option[Offer] = offers.find { offer => 
    val resources = Resources.fromOffer(offer)
    resources.cpus >= infoCpus && resources.memory >= infoMemory
  }

  def scalarResource(name: String, value: Double): Resource =
    Resource.newBuilder.setName(name).setType(Value.Type.SCALAR).setScalar(Value.Scalar.newBuilder().setValue(value)).build()

  def envVar(name: String, value: String) = Environment.Variable.newBuilder().setName(name).setValue(value).build()
  implicit def pairToVariable(p: (String, String)) = envVar(p._1, p._2)
  implicit def mapToEnvironment(map: Map[String, String]): Environment = {
    val builder = Environment.newBuilder()
    for ((key, value) <- map) builder.addVariables(key -> value)
    builder.build()
  }

  def createTaskInfoForComputingJobInfo(command: RunSamzaJob, offer: Offer): TaskInfo = {
    val taskId = s"${command.jobName}-compute-job-info-${UUID.randomUUID.toString}"

    val environment = Environment.newBuilder()
      .addVariables("HEATBLAST_HOST" -> publicHttpServerHost)
      .addVariables("HEATBLAST_PORT" -> httpServerPort.toString)
      .build()

    TaskInfo.newBuilder()
      .setTaskId(TaskID.newBuilder().setValue(taskId).build())
      .setName(taskId)
      .setSlaveId(offer.getSlaveId)
      .addResources(scalarResource("cpus", infoCpus))
      .addResources(scalarResource("mem", infoMemory))
      // .addResources(scalarResource("disk", config.getExecutorMaxDiskMb))
      .setCommand(CommandInfo.newBuilder()
        .setEnvironment(environment)
        .setShell(false)
        .addAllArguments(Seq("get-mesos-configs", command.jobName))
        .build())
      .setContainer(ContainerInfo.newBuilder().setType(ContainerInfo.Type.DOCKER).setDocker(ContainerInfo.DockerInfo.newBuilder().setImage(command.dockerImage).build()).build())
      .build()
  }

  def useOffersForComputingJobInfo(driver: SchedulerDriver, offers: Seq[Offer]): Seq[Offer] = {
    if (!infosToCompute.isEmpty) { //TODO this method needs to try to run all the compute info commands, not just one
      val command = infosToCompute.peek
      getOfferForComputingJobInfo(command, offers) match {
        case Some(offer) => 
          val taskInfo = createTaskInfoForComputingJobInfo(command, offer)
          log.info(s"Using offer ${offer.getId} to compute info for $command in task $taskInfo.getTaskId")
          driver.launchTasks(Seq(offer.getId), Seq(taskInfo))
          infosToCompute.remove(command) //peek above did not remove the command
          offers.filterNot(_.getId == offer.getId)
        case None => 
          log.info(s"None of the ${offers.size} offers were suitable for computing info for $command")
          offers
      }
    } else {
      offers
    }
  }

  def createTaskInfoForSamzaContainer(
      jobName: String, 
      dockerImage: String, 
      containerId: Int, 
      config: SamzaConfig, 
      sspTaskNames: TaskNamesToSystemStreamPartitions,
      taskNameToChangeLogPartitionMapping: Map[TaskName, Int],
      offer: Offer, 
      resources: Resources): TaskInfo = {
    val taskId = s"$jobName-container-$containerId"

    val commandBuilder = classOf[ShellCommandBuilder].newInstance.asInstanceOf[CommandBuilder]
      .setConfig(config)
      .setName(taskId)
      .setTaskNameToSystemStreamPartitionsMapping(sspTaskNames.getJavaFriendlyType)
      .setTaskNameToChangeLogPartitionMapping(taskNameToChangeLogPartitionMapping.mapValues(i => i: Integer))
    val environmentVariables = commandBuilder.buildEnvironment().asScala.toMap //TODO add any other env vars for samza container to this map

    TaskInfo.newBuilder()
      .setTaskId(TaskID.newBuilder().setValue(taskId).build())
      .setName(taskId)
      .setSlaveId(offer.getSlaveId)
      .addResources(scalarResource("cpus", resources.cpus))
      .addResources(scalarResource("mem", resources.memory))
      .setCommand(CommandInfo.newBuilder()
        .setEnvironment(environmentVariables)
        .setShell(false)
        .addAllArguments(Seq("run-container"))
        .build())
      .setContainer(ContainerInfo.newBuilder().setType(ContainerInfo.Type.DOCKER).setDocker(ContainerInfo.DockerInfo.newBuilder().setImage(dockerImage).build()).build())
      .build()
  }

  override def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo): Unit = {
    log.info(s"Registered frameworkdId $frameworkId")
  }

  override def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo): Unit = {
    log.info(s"Reregistered masterInfo $masterInfo")
  }

  override def resourceOffers(driver: SchedulerDriver, joffers: JavaList[Offer]): Unit = {
    val offers = joffers.asScala
    log.info(s"Received ${offers.size} offers")
    val unusedOffers = useOffersForComputingJobInfo(driver, offers)
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
