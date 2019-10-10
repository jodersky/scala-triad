package triad

class Templates[Builder, Output <: FragT, FragT](
    val bundle: scalatags.generic.Bundle[Builder, Output, FragT]
) {
  import bundle.all._

  val colorStyles = List(
    "bg-primary",
    "bg-secondary",
    "bg-success",
    "bg-danger",
    "bg-warning",
    "bg-info",
    "bg-dark"
  )
  // pick a "random" style by computing a hash of arbitrary data
  def dataStyle(data: String) = {
    val dataHash = data.foldLeft(7) {
      case (hash, char) =>
        (hash * 31 + char.toInt)
    }
    colorStyles(
      ((dataHash % colorStyles.length) + colorStyles.length) % colorStyles.length
    )
  }

  def message(msg: Message) = {
    val tags = msg.hashTags.map(
      hashTag =>
        span(`class` := "badge badge-light float-right ml-1")(
          hashTag
        )
    )
    div(`class` := "col-xs-12 col-sm-6 col-md-3 col-lg-2")(
      div(`class` := s"card text-white mb-3 ${dataStyle(msg.author)}")(
        div(`class` := "card-header")(
          msg.author,
          tags
        ),
        div(`class` := "card-body")(
          div(`class` := "card-text")(
            msg.content
          )
        )
      )
    )
  }

  def conversation(messages: Seq[Message]): Tag =
    div(`class` := "container-fluid")(
      div(id := "conversation", `class` := "row")(
        for (msg <- messages.sortBy(_.timestamp)) yield message(msg)
      )
    )

}
