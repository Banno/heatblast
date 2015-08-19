package com.banno.heatblast

import com.typesafe.config.Config
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

trait HttpServer extends SprayJsonSupport with Logging {
  def config: Config
  def samzaScheduler: SamzaMesosScheduler

  import HeatblastProtocol._

  //could refactor this out into an ActorModule trait if this stuff is also needed elsewhere
  implicit val system = ActorSystem("my-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val route =
    path("hello") {
      get {
        complete {
          "Say hello to Heatblast"
        }
      }
    } ~
    path("jobs") {
      get { complete { "TODO provide a list of all jobs" } } ~
      post {
        entity(as[RunSamzaJob]) { command =>
          log.info(command.toString)
          samzaScheduler.computeJobInfo(command)
          //get computed job info from zookeeper
          //run samza container mesos tasks
          complete { "TODO run the samza job..." }
        } ~
        entity(as[SamzaJobConfig]) { config =>
          log.info(s"Received config for job: $config")
          complete { "todo run samza container" }
        }
      }
    }

  def startHttpServer() = {
    lazy val host = config.getString("heatblast-scheduler.http-server.host")
    lazy val port = config.getInt("heatblast-scheduler.http-server.port")
    log.debug(s"Starting HTTP server on $host:$port...")
    val bindFuture = Http().bindAndHandle(route, host, port)
    bindFuture onSuccess { case _ => log.debug(s"Started HTTP server on $host:$port") }
    bindFuture
  }
}
