import com.banno._
import sbtdocker.DockerKeys

Docker.settings

Docker.baseImage in DockerKeys.docker := "banno/samza-mesos:0.22.1"
