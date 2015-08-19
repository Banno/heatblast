package com.banno.heatblast

import com.typesafe.config.Config
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

trait HttpServer extends Logging {
  def config: Config

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
