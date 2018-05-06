package triad

object TextTemplates extends Templates(scalatags.Text) {
  import bundle.all._

  def scripts(js: Boolean = true) =
    if (js)
      Seq(
        script(`type` := "text/javascript",
               src := "/assets/ui/js/ui-fastopt.js"),
        script(`type` := "text/javascript")(
          raw(
            """|document.addEventListener("DOMContentLoaded", function(event) {
               |  try {
               |    // root element that will contain the ScalaJS application
               |    var root = document.getElementById("conversation");
               |
               |    // clear any existing content
               |    while (root.firstChild) {
               |      root.removeChild(root.firstChild);
               |    }
               |
               |    // run ScalaJS application
               |    console.info("Starting ScalaJS application...")
               |    triad.Main().main(root)
               |  } catch(ex) {
               |    // display warning message in case of exception
               |    //document.getElementById("scalajs-error").style.display = "block";
               |    throw ex;
               |  }
               |});
               |""".stripMargin
          )
        )
      )
    else Seq.empty

  def page(messages: Seq[Message], js: Boolean = true) = html(
    head(),
    body(
      conversation(messages),
      scripts(js)
    )
  )

}
