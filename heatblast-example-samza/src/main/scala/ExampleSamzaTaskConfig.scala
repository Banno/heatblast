package com.banno.heatblast.example

import org.apache.samza.config.{Config, MapConfig}
import scala.collection.JavaConversions._

object ExampleSamzaTaskConfig {
  val samzaJobName = "ExampleSamzaTask"
  val samzaConfig = new MapConfig(map)

  // todo -- this config should be configged
  val map = Map(
    "mesos.master.connect" -> "zk://dev.banno.com:2181/mesos",
    "mesos.executor.count" -> "1",
    "mesos.executor.cpu.cores" -> "0.1",
    "mesos.executor.disk.mb" -> "",
    "mesos.scheduler.role" -> "",
    "mesos.docker.entrypoint.arguments" -> "container",

    "systems.kafka.producer.metadata.broker.list" -> "zk://dev.banno.com:2181/kafka",
    "systems.kafka.consumer.zookeeper.connect" -> "dev.banno.com:2181",

    "mesos.docker.image" -> "todo",

    "task.checkpoint.factory" -> "org.apache.samza.checkpoint.kafka.KafkaCheckpointManagerFactory",
    "task.checkpoint.system" -> "kafka",
    "task.checkpoint.replication.factor" -> "1",

    "systems.kafka.samza.factory" -> "org.apache.samza.system.kafka.KafkaSystemFactory",
    "systems.kafka.producer.batch.num.messages" -> "1",

    "serializers.registry.integer.class" -> "org.apache.samza.serializers.IntegerSerdeFactory",
    "serializers.registry.string.class" -> "org.apache.samza.serializers.StringSerdeFactory",

    "metrics.reporters" -> "",

    "job.id" -> "1",
    "job.name" -> samzaJobName,
    "task.class" -> "com.banno.heatblast.example.ExampleSamzaTask",
    "task.inputs" -> "heatblast-example-input"
  )
}
