package triad

object TextTemplates extends Templates(scalatags.Text) {
  import bundle.all._

  def scripts(js: Boolean = true) =
    if (js)
      Seq(
        div(id := "scalajs-error", style := "display: none;")(
          "ScalaJS raised an exception. See the log for more information."
        ),
        script(`type` := "text/javascript", src := "/out.js"),
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
               |    document.getElementById("scalajs-error").style.display = "block";
               |    throw ex;
               |  }
               |});
               |""".stripMargin
          )
        )
      )
    else Seq.empty

  def page(messages: Seq[Message], js: Boolean = true) = html(
    head(
      link(
        rel := "stylesheet",
        `type` := "text/css",
        href := "/assets/lib/bootstrap-4.1.0/css/bootstrap-reboot.min.css"
      ),
      link(
        rel := "stylesheet",
        `type` := "text/css",
        href := "/assets/lib/bootstrap-4.1.0/css/bootstrap-grid.min.css"
      ),
      link(
        rel := "stylesheet",
        `type` := "text/css",
        href := "/assets/lib/bootstrap-4.1.0/css/bootstrap.min.css"
      ),
      link(
        rel := "stylesheet",
        `type` := "text/css",
        href := "/assets/main.css"
      ),
      meta(
        name := "viewport",
        content := "width=device-width, initial-scale=1, shrink-to-fit=no"
      )
    ),
    body(
      conversation(messages),
      scripts(js)
    )
  )

}
