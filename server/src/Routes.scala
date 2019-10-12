package triad

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Source
import spray.json._
import triad.ApiProtocol._

import scala.concurrent.duration._

class Routes(repository: Repository, liveMessages: LiveMessages) {
  import repository.profile.api._

  // allows using scalatags templates as HTTP responses
  implicit val tagMarshaller: ToEntityMarshaller[scalatags.Text.Tag] = {
    Marshaller.stringMarshaller(MediaTypes.`text/html`).compose {
      (tag: scalatags.Text.Tag) =>
        tag.render
    }
  }

  private val lastMessages = repository.Messages.take(100).result

  // stream persisted messages first, followed by live ones
  private val messageStream: Source[Message, _] =
    Source
      .fromPublisher(repository.database.stream(lastMessages))
      .concat(liveMessages.feed)

  val messages = path("messages") {
    get {
      onSuccess(repository.database.run(lastMessages)) { messages =>
        complete(messages)
      }
    } ~ post {
      entity(as[Message]) { message =>
        extractExecutionContext { implicit ec =>
          val query = repository.Messages.insertOrUpdate(message)
          val action = repository.database.run(query).flatMap { _ =>
            liveMessages.push(message)
          }
          onSuccess(action) { _ =>
            complete(message)
          }
        }
      }
    }
  }

  val ui = pathEndOrSingleSlash {
    get {
      parameter("js".as[Boolean] ? true) { js =>
        onSuccess(repository.database.run(lastMessages)) { messages =>
          complete(TextTemplates.page(messages, js))
        }
      }
    }
  }

  val live = path("live") {
    get {
      val src = messageStream
        .map(msg => ServerSentEvent(msg.toJson.compactPrint))
        .keepAlive(10.seconds, () => ServerSentEvent.heartbeat)
      complete(src)
    }
  }

  val assets = pathPrefix("assets") {
    getFromResourceDirectory("assets")
  } ~ path("out.js") {
    getFromResource("out.js")
  }

  def all = messages ~ ui ~ live ~ assets

}
