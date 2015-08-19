import com.banno._
import sbtdocker.DockerKeys

Docker.settings

Docker.baseImage in DockerKeys.docker := "banno/samza-mesos:0.22.1"

//copied from banno-sbt-plugin's Samza module
Docker.entryPointPrelude in DockerKeys.docker := "LIBPROCESS_IP=`ifconfig eth0 | awk '/inet addr/ {gsub(\"addr:\", \"\", $2); print $2}'`"
