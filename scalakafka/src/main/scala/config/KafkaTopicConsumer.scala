package config

import observer.Subject
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}

import java.util.Properties
import java.time.Duration
import java.util
import scala.jdk.CollectionConverters._

class KafkaTopicConsumer extends Subject[ConsumerRecord[String, String]] {
  private var isRunning = false

  def consume(topic: String, poolingDuration: Long): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", KafkaTopicConsumer.BootstrapServers)
    props.put("key.deserializer", KafkaTopicConsumer.Deserializer)
    props.put("value.deserializer", KafkaTopicConsumer.Deserializer)
    props.put("auto.offset.reset", "latest")
    props.put("group.id", "ia-id")

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
    consumer.subscribe(util.Arrays.asList(topic))

    isRunning = true

    while (isRunning) {
      val records = consumer.poll(Duration.ofMillis(poolingDuration)).asScala

      for (record <- records) {
        println(s"Received: ${record.value}")
        notifyObservers(record)
      }
    }

    consumer.unsubscribe()
    consumer.close()
  }

  def cancel(): Unit = {
    isRunning = false
  }
}

object KafkaTopicConsumer {
  val BootstrapServers = "kafka-1:29092,kafka-2:39092"
  val Deserializer = "org.apache.kafka.common.serialization.StringDeserializer"
}