import com.banno._

Kafka.clients

Docker.settings

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "1.0",
  "org.apache.samza" %% "samza-kafka" % "0.8.0"
  // "org.apache.samza" %% "samza-kv-rocksdb" % "0.8.0"
)
