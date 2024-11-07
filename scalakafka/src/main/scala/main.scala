import config.{KafkaTopicConsumer, Producer}
import controler.BreakthroughController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object main {
  def main(args: Array[String]): Unit = {
    val breakthroughController = new BreakthroughController(
      new Producer(),
      new KafkaTopicConsumer()
    )

    breakthroughController.observeConsumer()

    val colorAssignationFuture = Future {
      breakthroughController.listenToColorAssignationTopic()
    }

    breakthroughController.sendIAId()

    Await.result(colorAssignationFuture, Duration.Inf)
  }
}
