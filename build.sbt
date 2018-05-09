// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released

import sbtcrossproject.{crossProject, CrossType}
import scalajscrossproject.ScalaJSCrossPlugin.autoImport.{
  toScalaJSGroupID => _,
  _
}

scalaVersion in ThisBuild := "2.12.6"
version in ThisBuild := {
  import sys.process._
  ("git describe --always --dirty=-SNAPSHOT --match v[0-9].*" !!).tail.trim
}
scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-language:_",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-encoding",
  "utf8"
)

lazy val common = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      "xyz.driver" %%% "spray-json-derivation" % "0.4.3",
      "com.lihaoyi" %%% "scalatags" % "0.6.7",
      "com.lihaoyi" %%% "utest" % "0.6.3" % "test"
    ),
    sourceGenerators in Compile += Def.task {
      val file = (sourceManaged in Compile).value / "scala" / "BuildInfo.scala"
      val content =
        s"""package triad
           |object BuildInfo {
           |  final val Version: String = "${version.value}"
           |}
           |""".stripMargin
      IO.write(file, content)
      Seq(file)
    }
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.2",
      "org.scala-js" %%% "scalajs-java-time" % "0.2.4"
    )
  )
  .nativeSettings(
    scalaVersion := "2.11.12",
    nativeLinkStubs := true,
    libraryDependencies ++= Seq(
      "io.crashbox" %%% "commando" % "0.1.2"
    ),
    sourceGenerators in Compile += Def.task {
      import sys.process._
      val file = (sourceManaged in Compile).value / "scala" / "NativeBuildInfo.scala"
      val content =
        s"""package triad
           |object NativeBuildInfo {
           |  final val Platform: String = 
           |    "${("uname -s" !!).trim}/${("uname -m" !!).trim}"
           |  final val NativeVersion: String = "${nativeVersion}"
           |}
           |""".stripMargin
      IO.write(file, content)
      Seq(file)
    }
  )

lazy val commonJS = common.js
lazy val commonJVM = common.jvm
lazy val commonNative = common.native

lazy val server = project
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % "2.5.11",
      "com.typesafe.akka" %% "akka-http" % "10.1.0",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0",
      "com.typesafe.slick" %% "slick" % "3.2.3",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "org.xerial" % "sqlite-jdbc" % "3.21.0.1"
    )
  )
  .dependsOn(commonJVM)
  .settings(Js.dependsOnJs(ui))

lazy val ui = project
  .enablePlugins(ScalaJSPlugin)
  .disablePlugins(RevolverPlugin)
  .dependsOn(commonJS)

lazy val client = project
  .enablePlugins(ScalaNativePlugin)
  .settings(
    scalaVersion := "2.11.12",
    nativeMode := "debug",
    name := "triad"
  )
  .dependsOn(commonNative)

lazy val root = (project in file("."))
  .aggregate(commonJS, commonJVM, commonNative, client, ui, server)
  .settings(
    publish := {},
    publishLocal := {}
  )

addCommandAlias("start", "reStart")
addCommandAlias("stop", "reStop")
