package com.banno.heatblast

import org.apache.samza.container.SamzaContainer
import org.apache.samza.config.Config

trait MesosTaskUtility extends SamzaJobStatePersistence {

  // /heatblast/jobs/jobName/jobInfo
  def generateConfigForMesosScheduler(jobName: String, config: Config) = {

  }

  def runSamzaContainer() = SamzaContainer.safeMain()
}
