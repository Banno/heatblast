package com.banno.heatblast

import spray.json._
import org.apache.samza.Partition
import org.apache.samza.util.Util
import org.apache.samza.container.{TaskName, TaskNamesToSystemStreamPartitions}
import org.apache.samza.system.SystemStreamPartition
import scala.collection.JavaConverters._

case class RunSamzaJob(jobName: String, dockerImage: String)

case class SamzaJobConfig(
  jobName: String,
  samzaConfig: Map[String, String],
  samzaContainerIdToSSPTaskNames: Map[Int, TaskNamesToSystemStreamPartitions],
  samzaTaskNameToChangeLogPartitionMapping: Map[TaskName, Int]
)

object HeatblastProtocol extends DefaultJsonProtocol {

  implicit val taskNameFormat = new JsonFormat[TaskName] {
    def write(name: TaskName): JsValue = name.getTaskName.toJson
    def read(js: JsValue): TaskName = js match {
      case JsString(s) => new TaskName(s)
      case other => deserializationError(s"Problem deserializing $js to a TaskName")
    }
  }

  implicit val systemStreamPartitionFormat = new JsonFormat[SystemStreamPartition] {
    def write(ssp: SystemStreamPartition): JsValue = JsObject(
      "system" -> ssp.getSystem.toJson,
      "stream" -> ssp.getStream.toJson,
      "partition" -> ssp.getPartition.getPartitionId.toJson
    )

    def read(js: JsValue): SystemStreamPartition = js.asJsObject.getFields("system", "stream", "partition") match {
      case Seq(JsString(system), JsString(stream), JsNumber(partition)) =>
        new SystemStreamPartition(system, stream, new Partition(partition.toInt))
      case other => deserializationError(s"Problem deserializing $js to SystemStreamPartition")
    }
  }

  implicit val taskNamesToSystemStreamPartitionsFormat = new JsonFormat[TaskNamesToSystemStreamPartitions] {
    def write(taskNamesToSystemStreamPartitions: TaskNamesToSystemStreamPartitions): JsValue = JsObject(
      taskNamesToSystemStreamPartitions.seq.toMap.map { case (taskName, systemStreamPartitions) =>
        taskName.getTaskName -> systemStreamPartitions.toJson
      }
    )
    def read(js: JsValue): TaskNamesToSystemStreamPartitions = js match {
      case JsObject(values) =>
        val entries = values.map { case (name, ssps) =>
          val taskName = new TaskName(name)
          val sspsSet = ssps.convertTo[Set[SystemStreamPartition]]
          taskName -> sspsSet.asJava
        }.asJava
        TaskNamesToSystemStreamPartitions(entries)

      case other => deserializationError(s"Problem deserializing $js to a TaskNamesToSystemStreamPartitions")
    }
  }

  implicit val runSamzaJobFormat = jsonFormat2(RunSamzaJob)
  implicit val samzaJobConfigFormat = jsonFormat4(SamzaJobConfig)
}
