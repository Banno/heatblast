package com.banno.heatblast

import spray.json.DefaultJsonProtocol

case class RunSamzaJob(jobName: String, dockerImage: String)
case class SamzaJobConfig(jobName: String, samzaConfig: Map[String, String])

object HeatblastProtocol extends DefaultJsonProtocol {
  implicit val runSamzaJobFormat = jsonFormat2(RunSamzaJob)
  implicit val samzaJobConfigFormat = jsonFormat2(SamzaJobConfig)
}
