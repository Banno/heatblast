package com.banno.heatblast.example

import org.apache.samza.config.Config

object SamzaConfigUtility {

  def getSamzaConfig(jobName: String): Option[Config] = samzaConfigsByJobName.get(jobName)

  private[this] lazy val samzaConfigsByJobName: Map[String, Config] =
    Map(ExampleSamzaTaskConfig.samzaJobName -> ExampleSamzaTaskConfig.samzaConfig)
}
