package com.banno.heatblast

import org.apache.mesos.state.{State, ZooKeeperState}
import com.typesafe.config.Config
import java.util.concurrent.TimeUnit
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import scala.concurrent.Future
import org.slf4j.LoggerFactory

// todo -- helpful methods for storing & retrieving running samza jobs

trait SamzaJobStatePersistence {

}

trait ZookeeperSamzaJobStatePersistence extends SamzaJobStatePersistence {

  def config: Config

  private[this] lazy val log = LoggerFactory.getLogger(this.getClass)

  private[this] def zkGet(id: String): Option[Any] = {
    val bytes = state.fetch(id).get.value
    if (bytes.size > 0) {
      try {
        val bais = new ByteArrayInputStream(bytes)
        val ois = new ObjectInputStream(bais)
        val value = ois.readObject()
        ois.close()
        Some(value)
      } catch {
        case e: Exception =>
          log.error(s"Problem serializing value with id $id. Ignoring.", e)
          None
      }
    } else {
      log.warn(s"Value $id not found in zookeeper. Ignoring.")
      None
    }
  }

  private[this] def zkSet(id: String, value: Any): Unit = {
    val variable = state.fetch(id).get
    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(value)
    val bytes = baos.toByteArray()
    oos.close()
    variable.mutate(bytes)
    state.store(variable)
  }

  private[this] lazy val state = new ZooKeeperState(config.getString("zookeeper.connect"),
                                                    config.getLong("zookeeper.timeout-seconds"),
                                                    TimeUnit.SECONDS,
                                                    config.getString("zookeeper.samza-mesos-scheduler-znode"))
}
