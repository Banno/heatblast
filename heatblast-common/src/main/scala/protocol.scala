package com.banno.heatblast

import spray.json.DefaultJsonProtocol
import org.apache.samza.container.{TaskName, TaskNamesToSystemStreamPartitions}

case class RunSamzaJob(jobName: String, dockerImage: String)
case class SamzaJobConfig(jobName: String, samzaConfig: Map[String, String])
case class SamzaJobConfig2(
 jobName: String,
 dockerImage: String,
 samzaConfig: Map[String, String],
 samzaContainerIdToSSPTaskNames: Map[Int, TaskNamesToSystemStreamPartitions],
 samzaTaskNameToChangeLogPartitionMapping: Map[TaskName, Int]
)

object HeatblastProtocol extends DefaultJsonProtocol {
  implicit val runSamzaJobFormat = jsonFormat2(RunSamzaJob)
  implicit val samzaJobConfigFormat = jsonFormat2(SamzaJobConfig)
}
