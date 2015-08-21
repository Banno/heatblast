package com.banno.heatblast

import org.apache.samza.util.Util
import org.apache.samza.container.{TaskName, TaskNamesToSystemStreamPartitions}
import org.apache.samza.config.{Config, MapConfig}
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory

object CreateSamzaJobConfig {

  private[this] lazy val log = LoggerFactory.getLogger(this.getClass)

  def apply(jobName: String, samzaConfig: Map[String, String]): SamzaJobConfig = {
    val config = new MapConfig(samzaConfig)
    val sspTaskNamesMapping = makeSamzaContainerIdToSSPTaskNames(config)
    val changelogPartitionMapping = makeSamzaTaskNameToChangeLogPartitionMapping(config, sspTaskNamesMapping)
    val jobConfig = SamzaJobConfig(jobName,
                                   samzaConfig,
                                   sspTaskNamesMapping,
                                   changelogPartitionMapping)
    log.info(s"Final job config being sent to scheduler: $jobConfig")
    jobConfig
  }


  def makeSamzaContainerIdToSSPTaskNames(config: Config): Map[Int, TaskNamesToSystemStreamPartitions] = {
    val result = Util.assignContainerToSSPTaskNames(config, getSamzaContainerCount(config))
    log.info(s"Mapping for containerIdToSSPTaskNames result: $result")
    result
  }

  def makeSamzaTaskNameToChangeLogPartitionMapping(config: Config, mapping: Map[Int, TaskNamesToSystemStreamPartitions]): Map[TaskName, Int] = {
    val result = Util.getTaskNameToChangeLogPartitionMapping(config, mapping)
    log.info(s"Mapping for taskNameToChangeLogPartition result: $result")
    result
  }

  def getSamzaContainerCount(config: Config) = {
    val count = config.getInt("mesos.executor.count")
    log.info(s"Samza container count from config: $count")
    count
  }
}
