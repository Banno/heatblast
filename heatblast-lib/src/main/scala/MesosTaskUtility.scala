package com.banno.heatblast

import org.apache.samza.container.SamzaContainer
import org.apache.samza.config.Config

object MesosTaskUtility {

  def submitSamzaConfigToMesosScheduler(jobName: String, config: Config) = {

  }

  def runSamzaContainer() = SamzaContainer.safeMain()
}
