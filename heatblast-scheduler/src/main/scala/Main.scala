package com.banno.heatblast

import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos.FrameworkInfo
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Main extends App { self =>

  val log = LoggerFactory.getLogger(this.getClass)
  val config = ConfigFactory.load()

  val frameworkInfo = FrameworkInfo.newBuilder().setUser("").setName("samza-scheduler").build()

  val samzaScheduler = new SamzaMesosScheduler with ZookeeperSamzaJobStatePersistence {
    val config = self.config
  }

  val mesosConnect = config.getString("mesos.master.connect")

  val driver = new MesosSchedulerDriver(samzaScheduler, frameworkInfo, mesosConnect)
  log.info(s"Running samza mesos scheduler on $mesosConnect")

  val exitCode = driver.run()
  log.info(s"Scheduler finished with exit code $exitCode")
}
