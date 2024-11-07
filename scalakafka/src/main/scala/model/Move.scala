package model

import play.api.libs.json._

case class Move(source: Point, target: Point)

object Move {
  implicit val pointFormat: Format[Point] = Json.format[Point]
  implicit val moveFormat: Format[Move] = Json.format[Move]
}
