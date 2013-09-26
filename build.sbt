name := "backend"

version := "0.0.1"

scalaVersion := "2.10.1"

libraryDependencies ++= Seq(
  "org.scalaz" % "scalaz-core_2.10" % "7.0.0-M9",
  "com.twitter" % "finagle-core_2.10" % "6.2.1",          
  "com.twitter" % "finagle-http_2.10" % "6.2.1",
  "com.typesafe.akka" % "akka-actor_2.10" % "2.1.2",
  "com.typesafe.akka" % "akka-zeromq_2.10.0-RC5" % "2.1.0-RC6"          
)
