package triad

import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent._
import scala.concurrent.duration._

object Main extends App {

  implicit val system = ActorSystem("triad")
  implicit val materializer = ActorMaterializer()

  val repository = {
    Files.deleteIfExists(Paths.get("database.sqlite"))
    Repository.sqlite("database.sqlite")
  }
  val liveMessages = new LiveMessages
  val routes = new Routes(repository, liveMessages)

  Await.result(repository.database.run(repository.initAction), 10.seconds)

  Await.result(Http().bindAndHandle(routes.all, "localhost", 9090), 10.seconds)

}
