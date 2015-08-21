package com.banno.heatblast

import org.apache.samza.container.SamzaContainer
import org.apache.samza.config.MapConfig
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.io.IO
import com.typesafe.config.ConfigFactory

object MesosTaskUtility {
  import spray.json._
  import DefaultJsonProtocol._
  import HeatblastProtocol._

  private[this] lazy val config = ConfigFactory.load()

  def runSamzaContainer() = SamzaContainer.safeMain()

  def submitSamzaConfigToMesosScheduler(jobName: String, samzaConfig: Map[String, String]) = {
    val samzaJobConfig = CreateSamzaJobConfig(jobName, samzaConfig)
    val payload = samzaJobConfig.toJson.toString
    sendSamzaConfigPayloadToScheduler(payload)
  }

  private[this] implicit lazy val system = ActorSystem("system")
  private[this] implicit lazy val materializer = ActorMaterializer()
  import system.dispatcher

  private[this] def sendSamzaConfigPayloadToScheduler(payload: String) = {
    val url = s"http://$apiHost:$apiPort/jobs"
    Http().singleRequest(HttpRequest(HttpMethods.POST, Uri(url), Nil, payload))
  }

  private[this] lazy val apiHost = config.getString("heatblast-scheduler.host")
  private[this] lazy val apiPort = config.getString("heatblast-scheduler.port")
}
