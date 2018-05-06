package triad

import java.time.Instant
import spray.json.{DerivedJsonProtocol, JsNumber, JsValue, JsonFormat}

object ApiProtocol extends DerivedJsonProtocol {
  implicit val timestampFormat: JsonFormat[Instant] = new JsonFormat[Instant] {
    def read(js: JsValue) = Instant.ofEpochMilli(js.convertTo[Long])
    def write(i: Instant) = JsNumber(i.toEpochMilli)
  }
  implicit val messageFormat = jsonFormat[Message]
}
