name := "backend"

version := "0.0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
//  "org.scalaz" %% "scalaz-core" % "7.0.3",
  "com.twitter" %% "finagle-core" % "6.6.2",          
  "com.twitter" %% "finagle-http" % "6.6.2",
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  "com.typesafe.akka" %% "akka-zeromq" % "2.2.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.1",
  "org.webbitserver" % "webbit" % "0.4.15",
  "asia.stampy" % "stampy-core" % "1.0-RELEASE",
  "org.scalatest" %% "scalatest" % "1.9.2" % "test",
  "com.github.scopt" %% "scopt" % "3.1.0",
//  "org.msgpack" %% "msgpack-scala" % "0.6.8",
  "org.msgpack" % "msgpack" % "0.6.8",
//  "com.twitter" % "ostrich" % "2.3.0"
  "com.codahale.metrics" % "metrics-core" % "3.0.1"
)
