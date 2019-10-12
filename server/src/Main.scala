package triad

import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent._
import scala.concurrent.duration._

object Main extends App {

  def log(message: String) = System.err.println(message)

  log("Initializing contexts...")
  implicit val system = ActorSystem("triad")
  implicit val materializer = ActorMaterializer()
  system.registerOnTermination {
    log("Bye!")
  }

  try {
    log("Initializing database...")
    val repository = {
      Files.deleteIfExists(Paths.get("database.sqlite"))
      Repository.sqlite("database.sqlite")
    }

    log("Preparing live message relay...")
    val liveMessages = new LiveMessages

    log("Setting up routes...")
    val routes = new Routes(repository, liveMessages)

    log("Populating database tables...")
    Await.result(repository.database.run(repository.initAction), 10.seconds)

    log("Binding to network...")
    Await.result(Http().bindAndHandle(routes.all, "0.0.0.0", 9090), 10.seconds)

    log("Ready")
  } catch {
    case ex: Exception =>
      log("Error in initialization. Shutting down...")
      system.terminate()
  }

}
