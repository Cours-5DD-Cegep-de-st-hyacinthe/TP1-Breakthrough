package config

import observer.Subject
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}

import java.util.Properties
import java.time.Duration
import java.util
import scala.jdk.CollectionConverters._

class KafkaTopicConsumer extends Subject[ConsumerRecord[String, String]] {
  def consume(topic: String): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", KafkaTopicConsumer.BootstrapServers)
    props.put("key.deserializer", KafkaTopicConsumer.Deserializer)
    props.put("value.deserializer", KafkaTopicConsumer.Deserializer)
    props.put("auto.offset.reset", "latest")
    props.put("group.id", "ia-id")

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(topic))

    while (true) {
      val records = consumer.poll(Duration.ofMillis(1000)).asScala

      for (record <- records) {
        notifyObservers(record)
      }
    }
  }
}

object KafkaTopicConsumer {
  val BootstrapServers = "kafka-1:29092,kafka-2:39092"
  val Deserializer = "org.apache.kafka.common.serialization.StringDeserializer"
}