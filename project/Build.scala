import sbt._
import Keys._
import Process._

object PokernoBuild extends Build {
  val pokernoVersion = "0.0.1"

  override lazy val settings = super.settings ++ Seq(
    organization := "de.pokerno",
    name := "pokerno",
    version := pokernoVersion,
    scalaVersion := "2.10.3"
  )

  lazy val deps = Seq(
    //"org.scalaz" %% "scalaz-core" % "7.0.3",
    "com.twitter" %% "finagle-core" % "6.6.2",          
    "com.twitter" %% "finagle-http" % "6.6.2",
    
    "com.typesafe.akka" %% "akka-actor" % "2.2.1",
    "com.typesafe.akka" %% "akka-zeromq" % "2.2.1",
    "com.typesafe.akka" %% "akka-testkit" % "2.2.1",
    
    "org.webbitserver" % "webbit" % "0.4.15",
    
    "asia.stampy" % "stampy-core" % "1.0-RELEASE",
    
    "com.github.scopt" %% "scopt" % "3.1.0",
    //"com.twitter" % "ostrich" % "2.3.0"
    "com.codahale.metrics" % "metrics-core" % "3.0.1",
    
    "org.msgpack" %% "msgpack-scala" % "0.6.8",
    //"org.msgpack" % "msgpack" % "0.6.8",
    "com.dyuproject.protostuff" % "protostuff-core" % "1.0.7",
    "com.dyuproject.protostuff" % "protostuff-json" % "1.0.7",
    "com.dyuproject.protostuff" % "protostuff-api" % "1.0.7",
    "com.dyuproject.protostuff" % "protostuff-runtime" % "1.0.7",
    "com.dyuproject.protostuff" % "protostuff-compiler" % "1.0.7"
  )
  
  lazy val testDeps = Seq(
    "org.scalatest" %% "scalatest" % "1.9.2" % "test"
  )
  
  lazy val root = Project(
    id = "pokerno",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      libraryDependencies ++= deps,
      libraryDependencies ++= testDeps
    )
  )
}
