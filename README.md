# Scala Triad
A demo webapp that uses Scala's coolest libraries and features. The
project is called "triad" as it includes parts written in all 3 of
Scala's target plaforms: JVM, JS, and Native.

The demo is a chat application and demontstrates how a full-fledged
web application can be built to be 100% typesafe, sharing a model
across backend, frontend and commandline utility.

It also showcases how an application can gracefuly degrade if a user
does not have javascript enabled.

## Features

- Server written in Scala
	- Akka HTTP for routing
	- Akka streams for safe concurrency abstractions
	- Slick for database access

- Scala Native as a commandline interface

- ScalaJS as an interactive frontend

- Shared model classes, utilities, and formats across all platforms
	- spray-json (derivation)
	- scalatags
