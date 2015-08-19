package com.banno.heatblast

import org.apache.samza.container.SamzaContainer
import org.apache.samza.config.MapConfig
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.io.IO

object MesosTaskUtility {
  import spray.json._
  import DefaultJsonProtocol._
  import HeatblastProtocol._

  def runSamzaContainer() = SamzaContainer.safeMain()

  // todo -- need the framework http endpoint
  def submitSamzaConfigToMesosScheduler(jobName: String, config: Map[String, String]) = {
    val samzaJobConfig = SamzaJobConfig(jobName, config)
    val frameworkHost = "todo"
    val payload = samzaJobConfig.toJson.toString
    sendSamzaConfigPayloadToScheduler(frameworkHost, payload)
  }

  private[this] implicit lazy val system = ActorSystem("system")
  private[this] implicit lazy val materializer = ActorMaterializer()
  import system.dispatcher

  private[this] def sendSamzaConfigPayloadToScheduler(frameworkEndpoint: String, payload: String) = {
    Http().singleRequest(HttpRequest(HttpMethods.POST, Uri(frameworkEndpoint), Nil, payload))
  }
}
