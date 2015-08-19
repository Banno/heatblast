package com.banno.heatblast.example

import org.apache.samza.config.Config

object SamzaConfigUtility {

  def getSamzaConfig(jobName: String): Option[Map[String, String]] = samzaConfigsByJobName.get(jobName)

  private[this] lazy val samzaConfigsByJobName: Map[String, Map[String, String]] =
    Map(ExampleSamzaTaskConfig.samzaJobName -> ExampleSamzaTaskConfig.map)
}
