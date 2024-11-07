package model

import play.api.libs.json._

case class IAStatusMessage(
                          id: String,
                          isReady: Boolean
                          )
// Companion object qui permet de définir le format de sérialisation/désérialisation
object IAStatusMessage {
  implicit val iaStatusMessageFormat: Format[IAStatusMessage] = Json.format[IAStatusMessage]
}
