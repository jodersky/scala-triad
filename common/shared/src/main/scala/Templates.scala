package triad

class Templates[Builder, Output <: FragT, FragT](
    val bundle: scalatags.generic.Bundle[Builder, Output, FragT]) {
  import bundle.all._

  def message(msg: Message) = li(
    div(`class` := "from")(msg.author),
    div(`class` := "content")(msg.content)
  )

  def conversation(messages: Seq[Message]): Tag = ul(id := "conversation")(
    for (msg <- messages.sortBy(_.timestamp)) yield message(msg)
  )

}
