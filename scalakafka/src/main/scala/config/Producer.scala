package config

import model.IAStatusMessage

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json.Json


class Producer {
  // Unit = type de retour, équivalent à void
  def sendIAStatusMessage(iaStatusMessage: IAStatusMessage): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", Producer.BootstrapServers)
    props.put("key.serializer", Producer.Serializer)
    props.put("value.serializer", Producer.Serializer)

    val producer = new KafkaProducer[String, String](props)
    val record = new ProducerRecord[String, String](
      Producer.IAStatusTopic,
      Json.toJson(iaStatusMessage).toString()
    )

    producer.send(record)
    producer.close()
  }
}

object Producer {
  val BootstrapServers = "kafka-1:29092,kafka-2:39092"
  val Serializer = "org.apache.kafka.common.serialization.StringSerializer"
  val IAStatusTopic = "IA-Status"
  val MoveResponseBlackTopic = "Move-Response-Black"
  val MoveResponseWhiteTopic = "Move-Response-White"
}
