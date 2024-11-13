package model

import play.api.libs.json.{Format, Json}

case class MoveRequestMessage(
                             timeLimit: Int,
                             board: Array[Array[Int]],
                             legalMoves: Array[Move]
                             )


object MoveRequestMessage {
  implicit val pointFormat: Format[Point] = Json.format[Point]
  implicit val moveFormat: Format[Move] = Json.format[Move]
  implicit val moveRequestMessageFormat: Format[MoveRequestMessage] = Json.format[MoveRequestMessage]
}
