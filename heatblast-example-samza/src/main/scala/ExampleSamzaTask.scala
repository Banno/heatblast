package com.banno.heatblast.example

import org.apache.samza.task.{StreamTask, MessageCollector, TaskCoordinator, InitableTask, TaskContext}
import org.apache.samza.system.{SystemStream, IncomingMessageEnvelope, OutgoingMessageEnvelope}
import org.slf4j.LoggerFactory

class ExampleSamzaTask extends StreamTask {

  val outputStream = new SystemStream("kafka", "heatblast-example-ouptut")
  val log = LoggerFactory.getLogger(this.getClass)

  def process(envelope: IncomingMessageEnvelope, collector: MessageCollector, coordinator: TaskCoordinator): Unit = {
    val message: String = envelope.getMessage().asInstanceOf[String]
    log.info(s"Received message in ExampleSamzaTask $message")
    collector.send(new OutgoingMessageEnvelope(outputStream, message))
  }
}
