package com.banno.heatblast.example

import com.banno.heatblast.{MesosTaskUtility, ZookeeperSamzaJobStatePersistence}
import com.typesafe.config.ConfigFactory

object Main extends App
    with MesosTaskUtility with ZookeeperSamzaJobStatePersistence {

  val config = ConfigFactory.load()

  if (askedToGetMesosSchedulerConfigs) {
    val jobName = getJobName()
    SamzaConfigUtility.getSamzaConfig(jobName).foreach { config =>
      generateConfigForMesosScheduler(jobName, config)
    }
  } else if (askedToRunSamzaContainer) {
    runSamzaContainer()
  } else {
    println(s"Whoops, invalid command! Args supplied: ${args.toList}")
  }

  def askedToGetMesosSchedulerConfigs() = args.size > 1 && args(0) == "get-mesos-configs"
  def askedToRunSamzaContainer() = args.size > 1 && args(0) == "run-container"

  def getJobName() = args(1)
}
