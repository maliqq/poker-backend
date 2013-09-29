name := "backend"

version := "0.0.1"

scalaVersion := "2.10.2"

autoCompilerPlugins := true

libraryDependencies <+= scalaVersion {
  v => compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.2")
}

scalacOptions += "-P:continuations:enable"

mainClass in (Compile, run) := Some("pokerno.backend.server.Main")

libraryDependencies ++= Seq(
  "org.scalaz" % "scalaz-core_2.10" % "7.0.3",
  "com.twitter" % "finagle-core_2.10" % "6.6.2",          
  "com.twitter" % "finagle-http_2.10" % "6.6.2",
  "com.typesafe.akka" % "akka-actor_2.10" % "2.2.1",
  "com.typesafe.akka" % "akka-zeromq_2.10" % "2.2.1",
  "com.typesafe.akka" %% "akka-dataflow" % "2.2.1",
  "asia.stampy" % "stampy-core" % "1.0-RELEASE",
  "org.scalatest" % "scalatest_2.10" % "1.9.2" % "test",
  "com.github.scopt" %% "scopt" % "3.1.0"
)
