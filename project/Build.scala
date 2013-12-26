import sbt._
import Keys._
import Process._
// sbt-assembly
import sbtassembly.Plugin._ 
import AssemblyKeys._

object PokernoBuild extends Build {
  val pokernoVersion = "0.0.1"

  override lazy val settings = super.settings ++ Seq(
    organization := "de.pokerno",
    scalaVersion := "2.10.3",
    resolvers += "spray repo" at "http://repo.spray.io"
    //,resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
  
  lazy val deps = Seq(
    //"org.scalaz" %% "scalaz-core" % "7.0.3",
    "com.typesafe.akka" %% "akka-actor" % "2.2.1",
    
    "org.webbitserver" % "webbit" % "0.4.15",
    
    "com.typesafe.akka" %% "akka-zeromq" % "2.2.1",
    "asia.stampy" % "stampy-core" % "1.0-RELEASE",
    //"asia.stampy" % "stampy-NETTY-client-server-RI" % "1.0-RELEASE",
    
    "com.github.scopt" %% "scopt" % "3.1.0",
    //"com.twitter" % "ostrich" % "2.3.0"
    "com.codahale.metrics" % "metrics-core" % "3.0.1",
    "org.codehaus.jackson" % "jackson-core-asl" % "1.9.13",
    
    "org.msgpack" %% "msgpack-scala" % "0.6.8",
    //"org.msgpack" % "msgpack" % "0.6.8",
    "com.dyuproject.protostuff" % "protostuff-core" % "1.0.7",
    "com.dyuproject.protostuff" % "protostuff-json" % "1.0.7",
    "com.dyuproject.protostuff" % "protostuff-runtime" % "1.0.7",
    
    "io.netty" % "netty-all" % "4.0.13.Final"
  )
  
  lazy val testDeps = Seq(
    "org.scalatest" %% "scalatest" % "1.9.2" % "test",
    "com.typesafe.akka" %% "akka-testkit" % "2.2.1"
  )
  
  lazy val root = Project(
    id = "pokerno",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno",
      version := "0.0.1",
      libraryDependencies ++= deps ++ testDeps
    ) ++ assemblySettings
  )

  lazy val ai = Project(
    id = "pokerno-ai",
    base = file("pokerno-ai"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-ai",
      version := "0.0.1"
    ) ++ assemblySettings
  ) dependsOn(root)
  
  lazy val bench = Project(
    id = "pokerno-bench",
    base = file("pokerno-bench"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-bench",
      version := "0.0.1"
    ) ++ assemblySettings
  ) dependsOn(root)
  
  lazy val cli = Project(
    id = "pokerno-cli",
    base = file("pokerno-cli"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-cli",
      version := "0.0.1"
    ) ++ assemblySettings
  ) dependsOn(root)
  
  lazy val server = Project(
    id = "pokerno-server",
    base = file("pokerno-server"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-server",
      version := "0.0.1" 
    ) ++ assemblySettings
  ) dependsOn(root)
  
}
