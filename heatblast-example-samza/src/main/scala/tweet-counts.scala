package com.banno.heatblast.example

import org.apache.samza.system.{SystemStream, IncomingMessageEnvelope, OutgoingMessageEnvelope}
import org.apache.samza.task.{StreamTask, MessageCollector, TaskCoordinator, InitableTask, TaskContext}
import org.apache.samza.storage.kv.KeyValueStore
import org.apache.samza.config.Config
import spray.json._
import org.slf4j.LoggerFactory

case class User(screen_name: String)
case class Tweet(user: User, text: String) // other interesting fields?


object TweetFormat extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat1(User)
  implicit val tweetFormat = jsonFormat2(Tweet)
}

// input: heatblast.incoming-tweets
// output: heatblast.tweets-by-username

class RepartitionTweetsByUsernameTask extends StreamTask {
  import TweetFormat._

  lazy val log = LoggerFactory.getLogger(this.getClass)

  lazy val outputStream = new SystemStream("kafka", "heatblast.tweets-by-username")

  def process(envelope: IncomingMessageEnvelope, collector: MessageCollector, coordinator: TaskCoordinator): Unit = {
    val msgBytes = envelope.getMessage().asInstanceOf[Array[Byte]]
    val msg = new String(msgBytes, "UTF-8")
    // log.debug(msg)
    // val js = msg.toJson.asJsObject()
    val js: JsObject = msg.parseJson.asJsObject()

    // just look for tweets containing "text" -- ignore other types of messages
    if (js.fields.exists(_._1 == "text")) {
      val tweet = js.convertTo[Tweet]
      collector.send(new OutgoingMessageEnvelope(outputStream, tweet.user.screen_name.getBytes("UTF-8"), msgBytes))
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
    val user = new String(envelope.getKey.asInstanceOf[Array[Byte]], "UTF-8")
    val count = Option(store.get(user)) getOrElse 0
    val updatedCount = count + 1
    log.info(s"User $user now has $updatedCount tweets!")
    store.put(user, updatedCount)
  }
}

object TweetJobConfigs {
  lazy val repartitionTweetsByUsernameJobName = "RepartitionTweetsByUsernameTask"

  lazy val repartitionTweetsByUsernameConfig = Map(
    "mesos.master.connect" -> "zk://dev.banno.com:2181/mesos",
    "mesos.executor.count" -> "1",
    "mesos.executor.cpu.cores" -> "0.1",
    "mesos.executor.disk.mb" -> "550",
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
    "mesos.executor.disk.mb" -> "550",
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
