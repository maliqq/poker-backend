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
    exportJars := true
    //,resolvers += "spray repo" at "http://repo.spray.io"
    //,resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
  
  lazy val deps = Seq(
    //"org.scalaz" %% "scalaz-core" % "7.0.3",
    "com.typesafe.akka" %% "akka-actor" % "2.2.1",
    //"com.twitter" % "ostrich" % "2.3.0"
    "com.twitter" %% "util-core" % "6.10.0",
    "com.codahale.metrics" % "metrics-core" % "3.0.1"
  )
  
  lazy val testDeps = Seq(
    "org.scalatest" %% "scalatest" % "1.9.2" % "test",
    "com.typesafe.akka" %% "akka-testkit" % "2.2.1"
  )

  lazy val protocol = Project(
    id = "pokerno-protocol",
    base = file("pokerno-protocol"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-protocol",
      version := "0.0.1",
      libraryDependencies ++= Seq(
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.3.0",
        "org.msgpack" %% "msgpack-scala" % "0.6.8",
        //"org.msgpack" % "msgpack" % "0.6.8",
        "com.dyuproject.protostuff" % "protostuff-core" % "1.0.7",
        "com.dyuproject.protostuff" % "protostuff-runtime" % "1.0.7"
      )
    ) ++ assemblySettings
  )
  
  lazy val core = Project(
    id = "pokerno-core",
    base = file("pokerno-core"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-core",
      version := "0.0.1",
      libraryDependencies ++= deps ++ testDeps
    ) ++ assemblySettings
  ) dependsOn(protocol)

  lazy val httpGateway = Project(
    id = "pokerno-gateway-http",
    base = file("pokerno-gateway-http"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-gateway-http",
      version := "0.0.1",
      libraryDependencies ++= deps ++ Seq(
        "io.netty" % "netty-all" % "4.0.14.Final"
      )
    ) ++ assemblySettings
  )

  lazy val stompGateway = Project(
    id = "pokerno-gateway-stomp",
    base = file("pokerno-gateway-stomp"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-gateway-stomp",
      version := "0.0.1",
      libraryDependencies ++= deps ++ Seq(
        "asia.stampy" % "stampy-core" % "1.0-RELEASE",
        //"asia.stampy" % "stampy-NETTY-client-server-RI" % "1.0-RELEASE",
        "io.netty" % "netty-all" % "4.0.14.Final"
      )
    ) ++ assemblySettings
  )

  lazy val backend = Project(
    id = "pokerno-backend",
    base = file("pokerno-backend"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-backend",
      version := "0.0.1",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-zeromq" % "2.2.1"
      )
    ) ++ assemblySettings
  ) dependsOn(core, httpGateway, stompGateway)

  lazy val ai = Project(
    id = "pokerno-ai",
    base = file("pokerno-ai"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-ai",
      version := "0.0.2",
      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % "3.1.0"
      )
    ) ++ assemblySettings
  ) dependsOn(core, backend)

  lazy val util = Project(
    id = "pokerno-util",
    base = file("pokerno-util"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-util",
      version := "0.0.1"
    ) ++ assemblySettings
  ) dependsOn(core)
  
  lazy val replay = Project(
    id = "pokerno-replay",
    base = file("pokerno-replay"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-replay",
      version := "0.0.2",
      libraryDependencies ++= Seq(
          "jline" % "jline" % "2.11",
          "com.github.scopt" %% "scopt" % "3.1.0"
        )
    ) ++ assemblySettings
  ) dependsOn(util, core, backend)
  
  lazy val bench = Project(
    id = "pokerno-bench",
    base = file("pokerno-bench"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-bench",
      version := "0.0.1"
    ) ++ assemblySettings
  ) dependsOn(core)
  
  lazy val cli = Project(
    id = "pokerno-cli",
    base = file("pokerno-cli"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-cli",
      version := "0.0.1",
      libraryDependencies ++= Seq(
        "jline" % "jline" % "2.11",
        "com.github.scopt" %% "scopt" % "3.1.0"
      )
    ) ++ assemblySettings
  ) dependsOn(core)
  
  lazy val server = Project(
    id = "pokerno-server",
    base = file("pokerno-server"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-server",
      version := "0.0.1",
      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % "3.1.0"
      )
    ) ++ assemblySettings
  ) dependsOn(core, backend)
  
}
