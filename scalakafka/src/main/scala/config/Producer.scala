package config

import model.{Constants, IAStatusMessage, Move}

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json.Json


class Producer {
  val props = new Properties()
  props.put("bootstrap.servers", Producer.BootstrapServers)
  props.put("key.serializer", Producer.Serializer)
  props.put("value.serializer", Producer.Serializer)

  val producer = new KafkaProducer[String, String](props)

  // Unit = type de retour, équivalent à void
  def sendIAStatusMessage(iaStatusMessage: IAStatusMessage): Unit = {
    println(s"Sending: ${Json.toJson(iaStatusMessage).toString()} in ${Producer.IAStatusTopic}")
    sendMessage (
      Producer.IAStatusTopic,
      Json.toJson(iaStatusMessage).toString()
    )
  }

  def sendMove(move: Move, color: Int): Unit = {
    if (color == Constants.unassignedColor) return

    val topic = if (color == Constants.whiteColor) Producer.MoveResponseWhiteTopic else Producer.MoveResponseBlackTopic

    println(s"Sending: ${Json.toJson(move).toString()} in ${topic}")
    sendMessage (
      topic,
      Json.toJson(move).toString()
    )
  }

  private def sendMessage(topic: String, message: String): Unit = {
    val record = new ProducerRecord[String, String] (topic, message)
    producer.send(record)
    producer.flush()
  }
}

object Producer {
  val BootstrapServers = "kafka-1:29092,kafka-2:39092"
  val Serializer = "org.apache.kafka.common.serialization.StringSerializer"
  val IAStatusTopic = "IA-Status"
  val MoveResponseBlackTopic = "Move-Response-Black"
  val MoveResponseWhiteTopic = "Move-Response-White"
}
