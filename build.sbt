name := "backend"

version := "0.0.1"

scalaVersion := "2.10.2"

mainClass in (Compile, run) := Some("pokerno.backend.server.Main")

libraryDependencies ++= Seq(
  "org.scalaz" % "scalaz-core_2.10" % "7.0.3",
  "com.twitter" % "finagle-core_2.10" % "6.6.2",          
  "com.twitter" % "finagle-http_2.10" % "6.6.2",
  "com.typesafe.akka" % "akka-actor_2.10" % "2.2.1",
  "com.typesafe.akka" % "akka-zeromq_2.10" % "2.2.1"
)
