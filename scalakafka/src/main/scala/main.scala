import config.{KafkaTopicConsumer, Producer}
import controler.BreakthroughController

object main {
  def main(args: Array[String]): Unit = {
    val breakthroughController = new BreakthroughController(
      new Producer(),
      new KafkaTopicConsumer(),
      new KafkaTopicConsumer()
    )

    breakthroughController.observeConsumer()

    breakthroughController.sendIAId()

    breakthroughController.listenToColorAssignationTopic()
  }
}
