import com.banno._

BannoSettings.settings

libraryDependencies ++= {
  val samzaVersion = "0.8.0"
  val mesosVersion = "0.22.1"
  Seq(
    "org.apache.samza" % "samza-api" % samzaVersion,
    "org.apache.samza" %% "samza-core" % samzaVersion,
    "org.apache.mesos" % "mesos" % mesosVersion,
    "com.typesafe" % "config" % "1.2.1"
  )
}

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "1.0"
