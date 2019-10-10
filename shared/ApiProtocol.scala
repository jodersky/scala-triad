package triad

import java.time.Instant
import spray.json.{
  DefaultJsonProtocol,
  JsNumber,
  JsValue,
  JsonFormat,
  RootJsonFormat
}

object ApiProtocol extends DefaultJsonProtocol {
  implicit val timestampFormat: JsonFormat[Instant] = new JsonFormat[Instant] {
    def read(js: JsValue) = Instant.ofEpochMilli(js.convertTo[Long])
    def write(i: Instant) = JsNumber(i.toEpochMilli)
  }

  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat(
    Message.apply _,
    "content",
    "author",
    "timestamp"
  )
}
