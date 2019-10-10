package triad

import ApiProtocol._
import http.Request
import commando._
import spray.json._

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.control.NonFatal

object Main {

  val command: Command = cmd("triad")(
    opt("server", 's', "url" -> true),
    opt("verbose", 'v')
  ).sub(
    cmd("message")(
      opt("author", param = "name" -> true),
      pos("content")
    ).run { args =>
      val server =
        args.get("server").map(_.head).getOrElse("http://localhost:9090")
      val author = args.get("author").map(_.head).getOrElse(sys.env("USER"))
      val content = args("content").head
      val verbose = args.get("verbose").map(_ => true).getOrElse(false)

      val message = Message(content, author).toJson.compactPrint

      val req = Request(
        "POST",
        s"$server/messages",
        Map("Content-type" -> "application/json"),
        message.getBytes("utf-8")
      )

      if (verbose) {
        System.err.println(req.url)
        System.err.println(message)
      }

      try {
        Await.result(http.send(req), 10.seconds) match {
          case resp if 200 <= resp.statusCode && resp.statusCode <= 300 =>
            sys.exit(0)
          case resp =>
            System.err.println(
              s"Bad response code while posting message ${resp.statusCode}."
            )
            sys.exit(1)
        }
      } catch {
        case NonFatal(e) =>
          System.err.println(e.getMessage)
          sys.exit(1)
      }
    },
    cmd("completion")().run { _ =>
      System.out.println(command.completion)
    }
  )

  def main(args: Array[String]): Unit = commando.parse(args, command)

}
