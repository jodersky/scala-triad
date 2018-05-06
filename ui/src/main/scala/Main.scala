package triad

import spray.json._
import ApiProtocol._
import scalajs.js
import org.scalajs.dom
import org.scalajs.dom.html

@js.annotation.JSExport
object Main {

  @js.annotation.JSExport
  def main(root: html.Element): Unit = {
    val source = new dom.EventSource("live")

    source.onmessage = (e: dom.MessageEvent) => {
      val str = e.data.asInstanceOf[String]
      if (str.nonEmpty) { // ignore empty strings on heartbeats
        println(str)
        val message = str.parseJson.convertTo[Message]
        val template = JsTemplates.message(message)
        root.appendChild(template.render)
        dom.window
          .scrollTo(0, dom.document.body.scrollHeight) // scroll to bottom
      }
    }

  }

}
