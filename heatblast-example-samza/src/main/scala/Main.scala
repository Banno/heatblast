package com.banno.heatblast.example

import com.banno.heatblast.MesosTaskUtility

object Main extends App {

  if (askedToGetMesosSchedulerConfigs) {
    val jobName = getJobName()
    SamzaConfigUtility.getSamzaConfig(jobName).foreach { configMap =>
      MesosTaskUtility.submitSamzaConfigToMesosScheduler(jobName, configMap)
    }
    //TODO may need so System.exit here?
  } else if (askedToRunSamzaContainer) {
    MesosTaskUtility.runSamzaContainer()
  } else {
    println(s"Whoops, invalid command! Args supplied: ${args.toList}")
  }

  def askedToGetMesosSchedulerConfigs() = args.size > 1 && args(0) == "get-mesos-configs"
  def askedToRunSamzaContainer() = args.size == 1 && args(0) == "run-container"

  def getJobName() = args(1)
}
