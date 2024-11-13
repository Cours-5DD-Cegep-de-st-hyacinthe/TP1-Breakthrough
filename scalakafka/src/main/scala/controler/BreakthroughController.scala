package controler

import config.{KafkaTopicConsumer, Producer}
import controler.BreakthroughController.IAId
import model.{ColorAssignationMessage, Constants, IAStatusMessage, MoveRequestMessage}
import observer.Observer
import org.apache.kafka.clients.consumer.ConsumerRecord
import play.api.libs.json.{JsError, JsSuccess, Json}
import service.BreakthroughService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BreakthroughController(
                              producer: Producer,
                              colorAssignationTopicConsumer: KafkaTopicConsumer,
                              moveRequestTopicConsumer: KafkaTopicConsumer
                            ) extends Observer[ConsumerRecord[String, String]] {
  private var _color = Constants.unassignedColor

  def sendIAId(): Unit = {
    producer.sendIAStatusMessage(new IAStatusMessage(BreakthroughController.IAId, true))
  }

  def observeConsumer(): Unit = {
    colorAssignationTopicConsumer.addObserver(this)
    moveRequestTopicConsumer.addObserver(this)
  }

  def listenToColorAssignationTopic(): Unit = {
    colorAssignationTopicConsumer.consume(BreakthroughController.ColorAssignationTopic, 1000)
  }

  private def listenToWhiteMoveRequestTopic(): Unit = {
    moveRequestTopicConsumer.consume(BreakthroughController.MoveRequestWhiteTopic, 1000)
  }

  private def listenToBlackMoveRequestTopic(): Unit = {
    moveRequestTopicConsumer.consume(BreakthroughController.MoveRequestBlackTopic, 1000)
  }

  override def receiveUpdate(record: ConsumerRecord[String, String]): Unit = {
    record.topic() match {
      case BreakthroughController.ColorAssignationTopic => consumeColorAssignation(deserializeColorAssignationMessage(record.value()))
      case BreakthroughController.MoveRequestBlackTopic => consumeMoveRequest(deserializeMoveRequestMessage(record.value()))
      case BreakthroughController.MoveRequestWhiteTopic => consumeMoveRequest(deserializeMoveRequestMessage(record.value()))
    }
  }

  def consumeColorAssignation(colorAssignation: ColorAssignationMessage): Unit = {
    if (colorAssignation.idIA != IAId) return

    _color = colorAssignation.color

    if (colorAssignation.color == Constants.unassignedColor) {
      moveRequestTopicConsumer.cancel()
    } else if (colorAssignation.color == Constants.whiteColor) {
      Future {
        listenToWhiteMoveRequestTopic()
      }
    } else {
      Future {
        listenToBlackMoveRequestTopic()
      }
    }
  }

  def consumeMoveRequest(moveRequest: MoveRequestMessage): Unit = {
    producer.sendMove(BreakthroughService.chooseMove(moveRequest.legalMoves), _color)
  }

  def deserializeColorAssignationMessage(colorAssignationString: String): ColorAssignationMessage = {
    val colorAssignationResult = Json.parse(colorAssignationString).validate[ColorAssignationMessage]
    colorAssignationResult match {
      case JsSuccess(colorAssignation, _) => println(s"Deserialized color assignation")
      case e: JsError         => println(s"Errors: ${JsError.toJson(e)}")
    }

    colorAssignationResult.getOrElse(null)
  }

  def deserializeMoveRequestMessage(colorAssignationString: String): MoveRequestMessage = {
    val moveRequestResult = Json.parse(colorAssignationString).validate[MoveRequestMessage]
    moveRequestResult match {
      case JsSuccess(moveRequest, _) => println(s"Deserialized move request")
      case e: JsError         => println(s"Errors: ${JsError.toJson(e)}")
    }

    moveRequestResult.getOrElse(null)
  }
}

object BreakthroughController {
  val IAId = "Ia-Exemple"
  val ColorAssignationTopic = "Color-Assignation"
  val MoveRequestBlackTopic = "Move-Request-Black"
  val MoveRequestWhiteTopic = "Move-Request-White"
}
