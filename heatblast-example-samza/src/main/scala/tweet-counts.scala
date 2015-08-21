package com.banno.heatblast.example

import org.apache.samza.system.{SystemStream, IncomingMessageEnvelope, OutgoingMessageEnvelope}
import org.apache.samza.task.{StreamTask, MessageCollector, TaskCoordinator, InitableTask, TaskContext}
import org.apache.samza.storage.kv.KeyValueStore
import org.apache.samza.config.Config
import spray.json._
import DefaultJsonProtocol._

case class Tweet(screen_name: String, text: String) // other interesting fields?

object TweetFormat extends DefaultJsonProtocol {
  implicit val tweetFormat = jsonFormat2(Tweet)
}

// input: heatblast.incoming-tweets
// output: heatblast.tweets-by-username

class RepartitionTweetsByUsernameTask extends StreamTask {
  import TweetFormat._

  lazy val outputStream = new SystemStream("kafka", "heatblast.tweets-by-username")

  def process(envelope: IncomingMessageEnvelope, collector: MessageCollector, coordinator: TaskCoordinator): Unit = {
    val msg = envelope.getMessage().asInstanceOf[String]
    val js = msg.toJson.asJsObject()

    // just look for tweets containing "text" -- ignore other types of messages
    if (js.fields.exists(_._1 == "text")) {
      val tweet = js.convertTo[Tweet]
      collector.send(new OutgoingMessageEnvelope(outputStream, tweet.screen_name, 1))
    }
  }
}

// input: heatblast.tweets-by-username
// output (in store changelog): heatblast.tweet-counts-by-username

class CountTweetsByUsernameTask extends StreamTask with InitableTask {

  lazy val storeName = "heatblast.tweet-counts-by-username"
  var store: KeyValueStore[String, Int] = null

  def init(config: Config, context: TaskContext): Unit = {
    store = context.getStore(storeName).asInstanceOf[KeyValueStore[String, Int]]
  }

  def process(envelope: IncomingMessageEnvelope, collector: MessageCollector, coordinator: TaskCoordinator): Unit = {
    val user = envelope.getKey.asInstanceOf[String]
    val count = Option(store.get(user)) getOrElse 0
    store.put(user, count + 1)
  }
}

object TweetJobConfigs {
  lazy val repartitionTweetsByUsernameJobName = "RepartitionTweetsByUsernameTask"

  lazy val repartitionTweetsByUsernameConfig = Map(
    "mesos.master.connect" -> "zk://dev.banno.com:2181/mesos",
    "mesos.executor.count" -> "1",
    "mesos.executor.cpu.cores" -> "0.1",
    "mesos.executor.disk.mb" -> "25",
    "mesos.scheduler.role" -> "samza",
    "mesos.docker.entrypoint.arguments" -> "container",

    "systems.kafka.producer.metadata.broker.list" -> "dev.banno.com:9092",
    "systems.kafka.consumer.zookeeper.connect" -> "dev.banno.com:2181/kafka",

    "task.checkpoint.factory" -> "org.apache.samza.checkpoint.kafka.KafkaCheckpointManagerFactory",
    "task.checkpoint.system" -> "kafka",
    "task.checkpoint.replication.factor" -> "1",

    "systems.kafka.samza.factory" -> "org.apache.samza.system.kafka.KafkaSystemFactory",
    "systems.kafka.producer.batch.num.messages" -> "1",

    "serializers.registry.integer.class" -> "org.apache.samza.serializers.IntegerSerdeFactory",
    "serializers.registry.string.class" -> "org.apache.samza.serializers.StringSerdeFactory",

    "metrics.reporters" -> "",

    "job.id" -> "1",
    "job.name" -> repartitionTweetsByUsernameJobName,
    "task.class" -> "com.banno.heatblast.example.RepartitionTweetsByUsernameTask",
    "task.inputs" -> "kafka.heatblast.incoming-tweets"
  )

  lazy val countTweetsByUsernameJobName = "CountTweetsByUsernameTask"

  lazy val countTweetsByUsername = Map(
    "mesos.master.connect" -> "zk://dev.banno.com:2181/mesos",
    "mesos.executor.count" -> "1",
    "mesos.executor.cpu.cores" -> "0.1",
    "mesos.executor.disk.mb" -> "25",
    "mesos.scheduler.role" -> "samza",
    "mesos.docker.entrypoint.arguments" -> "container",

    "systems.kafka.producer.metadata.broker.list" -> "dev.banno.com:9092",
    "systems.kafka.consumer.zookeeper.connect" -> "dev.banno.com:2181/kafka",

    "task.checkpoint.factory" -> "org.apache.samza.checkpoint.kafka.KafkaCheckpointManagerFactory",
    "task.checkpoint.system" -> "kafka",
    "task.checkpoint.replication.factor" -> "1",

    "systems.kafka.samza.factory" -> "org.apache.samza.system.kafka.KafkaSystemFactory",
    "systems.kafka.producer.batch.num.messages" -> "1",

    "serializers.registry.integer.class" -> "org.apache.samza.serializers.IntegerSerdeFactory",
    "serializers.registry.string.class" -> "org.apache.samza.serializers.StringSerdeFactory",

    "stores.heatblast.tweet-counts-by-username.changelog" -> "kafka.heatblast.tweet-counts-by-username",
    "stores.heatblast.tweet-counts-by-username.factory" -> "org.apache.samza.storage.kv.RocksDbKeyValueStorageEngineFactory",
    "stores.heatblast.tweet-counts-by-username.key.serde" -> "string",
    "stores.heatblast.tweet-counts-by-username.msg.serde" -> "integer",
    "stores.heatblast.tweet-counts-by-username.object.cache.size" -> "0",

    "metrics.reporters" -> "",

    "job.id" -> "1",
    "job.name" -> countTweetsByUsernameJobName,
    "task.class" -> "com.banno.heatblast.example.CountTweetsByUsernameTask",
    "task.inputs" -> "kafka.heatblast.tweets-by-username"
  )
}
