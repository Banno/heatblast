package com.banno.heatblast

import com.typesafe.config.Config
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Job(name: String)

object JobProtocol extends DefaultJsonProtocol {
  implicit val jobFormat = jsonFormat1(Job)
}

trait HttpServer extends SprayJsonSupport with Logging with HeatblastConfig { self: SamzaMesosScheduler =>
  import HeatblastProtocol._
  import JobProtocol._

  //could refactor this out into an ActorModule trait if this stuff is also needed elsewhere
  implicit val system = ActorSystem("my-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val route =
    path("hello") {
      get {
        complete {
          runningJobs.head
        }
      }
    } ~
    path("jobs") {
      get { 
        complete { 
          "TODO provide a list of all jobs"
        } 
      } ~
      post {
        entity(as[RunSamzaJob]) { command =>
          log.info(command.toString)
          computeJobInfo(command)
          //get computed job info from zookeeper
          //run samza container mesos tasks
          complete { s"Creating samza job config for $command" }
        } ~
        entity(as[SamzaJobConfig]) { config =>
          log.info(s"Received config for job: $config")
          runJob(config)
          complete { s"Ran job for $config" }
        }
      }
    }

  def startHttpServer() = {
    log.debug(s"Starting HTTP server on $httpServerHost:$httpServerPort...")
    val bindFuture = Http().bindAndHandle(route, httpServerHost, httpServerPort)
    bindFuture onSuccess { case _ => log.debug(s"Started HTTP server on $httpServerHost:$httpServerPort") }
    bindFuture
  }
}
