package controler

import config.{KafkaTopicConsumer, Producer}
import model.{ColorAssignationMessage, IAStatusMessage}
import observer.Observer
import org.apache.kafka.clients.consumer.ConsumerRecord
import play.api.libs.json.{JsError, JsSuccess, Json}

class BreakthroughController(producer: Producer, consumer: KafkaTopicConsumer) extends Observer[ConsumerRecord[String, String]] {

  def observeConsumer(): Unit = {
    consumer.addObserver(this)
  }

  def listenToColorAssignationTopic(): Unit = {
    consumer.consume(BreakthroughController.ColorAssignationTopic)
  }

  def sendIAId(): Unit = {
    producer.sendIAStatusMessage(new IAStatusMessage(BreakthroughController.IAId, true))
  }

  override def receiveUpdate(record: ConsumerRecord[String, String]): Unit = {
    record.topic() match {
      case BreakthroughController.ColorAssignationTopic => consumeColorAssignation(deserializeColorAssignationMessage(record.value()))
    }
  }

  def consumeColorAssignation(colorAssignation: ColorAssignationMessage): Unit = {
    println(s"Couleur reçue: ${colorAssignation.color}")
  }

  def deserializeColorAssignationMessage(colorAssignationString: String): ColorAssignationMessage = {
    val colorAssignationResult = Json.parse(colorAssignationString).validate[ColorAssignationMessage]
    colorAssignationResult match {
      case JsSuccess(colorAssignation, _) => println(s"Name: $colorAssignation")
      case e: JsError         => println(s"Errors: ${JsError.toJson(e)}")
    }

    colorAssignationResult.getOrElse(null)
  }
}

object BreakthroughController {
  val IAId = "Ia-Exemple"
  val ColorAssignationTopic = "Color-Assignation"
  val MoveRequestBlackTopic = "Move-Request-Black"
  val MoveRequestWhiteTopic = "Move-Request-White"
}
