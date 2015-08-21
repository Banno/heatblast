package com.banno.heatblast

import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos.FrameworkInfo
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

trait Logging {
  lazy val log = LoggerFactory.getLogger(this.getClass)
}

trait HeatblastConfig {
  lazy val config = ConfigFactory.load()

  lazy val httpServerHost = config.getString("heatblast-scheduler.http-server.host")
  lazy val httpServerPort = config.getInt("heatblast-scheduler.http-server.port")

  lazy val publicHttpServerHost = config.getString("heatblast-scheduler.http-server.public-host")
}

object Main extends App with HttpServer with Logging with HeatblastConfig { self =>
  val frameworkInfo = FrameworkInfo.newBuilder().setUser("").setName("samza-scheduler").build()

  val samzaScheduler = new SamzaMesosScheduler with ZookeeperSamzaJobStatePersistence

  val mesosConnect = config.getString("mesos.master.connect")

  val driver = new MesosSchedulerDriver(samzaScheduler, frameworkInfo, mesosConnect)
  log.info(s"Running samza mesos scheduler on $mesosConnect")

  startHttpServer()

  val exitCode = driver.run()
  log.info(s"Scheduler finished with exit code $exitCode")
}
