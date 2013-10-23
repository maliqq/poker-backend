name := "backend"

version := "0.0.1"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.scalaz" % "scalaz-core_2.10" % "7.0.3",
  "com.twitter" % "finagle-core_2.10" % "6.6.2",          
  "com.twitter" % "finagle-http_2.10" % "6.6.2",
  "com.typesafe.akka" % "akka-actor_2.10" % "2.2.1",
  "com.typesafe.akka" % "akka-zeromq_2.10" % "2.2.1",
  "com.typesafe.akka" % "akka-testkit_2.10" % "2.2.1",
  "org.webbitserver" % "webbit" % "0.4.15",
  "asia.stampy" % "stampy-core" % "1.0-RELEASE",
  "org.scalatest" % "scalatest_2.10" % "1.9.2" % "test",
  "com.github.scopt" %% "scopt" % "3.1.0",
  //"com.twitter" % "ostrich" % "2.3.0"
  "com.codahale.metrics" % "metrics-core" % "3.0.1"
)
