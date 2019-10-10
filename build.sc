import mill._, scalalib._, scalajslib._, scalanativelib._, scalafmt._

trait Shared extends ScalaModule with ScalafmtModule{

  def sharedSources = T.sources(build.millSourcePath / "shared")

  def sources = T.sources(
    super.sources() ++ sharedSources()
  )

  def ivyDeps = Agg(
    ivy"io.crashbox::spray-json::1.3.5-6",
    ivy"com.lihaoyi::scalatags::0.7.0"
  )

}

object server extends ScalaModule with Shared {
  def scalaVersion = "2.13.1"

  def ivyDeps = T {
    super.ivyDeps() ++ Agg(
      ivy"com.typesafe.akka::akka-stream:2.5.25",
      ivy"com.typesafe.akka::akka-http:10.1.10",
      ivy"com.typesafe.akka::akka-http-spray-json:10.1.10",
      ivy"com.typesafe.slick::slick:3.3.2",
      ivy"org.slf4j:slf4j-nop:1.6.4",
      ivy"org.xerial:sqlite-jdbc:3.28.0"
    )
  }

  // This includes the resulting javascript file so that it can be served
  // as a classpath resource and is packaged in the final jar.
  def localClasspath = T{
    super.localClasspath() :+ PathRef(ui.fastOpt().path / os.up)
  }

}

object ui extends ScalaJSModule with Shared {
  def scalaVersion = "2.13.1"
  def scalaJSVersion = "0.6.29"

  def ivyDeps = T {
    super.ivyDeps() ++ Agg(
      ivy"org.scala-js::scalajs-dom::0.9.7",
      ivy"org.scala-js::scalajs-java-time::0.2.5"
    )
  }

}

object client extends ScalaNativeModule with Shared {
  import scalanativelib.api.ReleaseMode
  
  def scalaVersion = "2.11.12"
  def scalaNativeVersion = "0.3.8"
  
  def releaseMode = ReleaseMode.Debug

  def ivyDeps = T {
    super.ivyDeps() ++ Agg(
      ivy"io.crashbox::commando::0.1.2"
    )
  }
}

def dist = T {
  server.assembly()
  client.nativeLink()
}
