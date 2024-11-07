package model

import play.api.libs.json._

case class ColorAssignationMessage(
                                  idIA: String,
                                  color: Int
                                  )

object ColorAssignationMessage {
  implicit val colorAssignationMessageFormat: Format[ColorAssignationMessage] = Json.format[ColorAssignationMessage]
}
