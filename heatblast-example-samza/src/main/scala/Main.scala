package com.banno.heatblast.example

import com.banno.heatblast.MesosTaskUtility
import org.slf4j.LoggerFactory
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  private[this] lazy val log = LoggerFactory.getLogger(this.getClass)

  if (askedToGetMesosSchedulerConfigs) {
    val jobName = getJobName()
    val samzaConfig = SamzaConfigUtility.getSamzaConfig(jobName)

    if (samzaConfig.isEmpty) {
      log.error(s"For some reason, the samza config generation returned none for $jobName")
      System.exit(1)
    }

    samzaConfig.foreach { configMap =>
      val fSubmission = MesosTaskUtility.submitSamzaConfigToMesosScheduler(jobName, configMap)
      fSubmission.onComplete {
        case Success(_) =>
          log.info("Job config sent successfully! Time to die.")
          System.exit(0)
        case Failure(e) =>
          log.error(s"Oh no, the job config submission failed: $e")
          System.exit(1)
      }
    }

  } else if (askedToRunSamzaContainer) {
    MesosTaskUtility.runSamzaContainer()
  } else {
    println(s"Whoops, invalid command! Args supplied: ${args.toList}")
  }

  def askedToGetMesosSchedulerConfigs() = args.size > 1 && args(0) == "get-mesos-configs"
  def askedToRunSamzaContainer() = args.size == 1 && args(0) == "run-container"

  def getJobName() = args(1)
}
