import sbt._
import Keys._
import Process._
// sbt-assembly
import sbtassembly.Plugin._ 
import AssemblyKeys._
import com.typesafe.sbt.SbtGit._
import com.twitter.scrooge.ScroogeSBT

object GitVersionStrategy extends Plugin {

  def gitVersion: Seq[Setting[_]] = Seq(
    version in ThisBuild <<= (Default.gitCurrentBranch, Default.gitHeadCommit) apply Default.makeVersion
  )

  object Default {
    val gitHeadCommit = GitKeys.gitHeadCommit in ThisBuild
    val gitCurrentTags = GitKeys.gitCurrentTags in ThisBuild
    val gitCurrentBranch = GitKeys.gitCurrentBranch in ThisBuild

    def makeVersion(currentBranch: String, headCommit: Option[String]) = {
      def rcVersion: Option[String] = {
        val releasePattern = "release-((?:\\d+\\.)+(?:\\d+))".r
        currentBranch match {
          case releasePattern(rcVersion) =>
            Some(rcVersion + "-" + dateVersionPart + "-" + headCommitPart.get)
          case _ =>
            Some("SNAPSHOT")
        }
      }

      def headCommitPart: Option[String] = headCommit map (sha => sha take 7)

      def dateVersionPart = {
        val df = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")
        df setTimeZone java.util.TimeZone.getTimeZone("GMT")
        df format (new java.util.Date)
      }
      
      rcVersion.get
    }
  }
}

import GitVersionStrategy._

object PokernoBuild extends Build {
  val pokernoVersion  = "0.0.1"
  val scoptVersion    = "3.1.0"
  val nettyVersion    = "4.0.19.Final"
  val akkaVersion     = "2.2.3"

  override lazy val settings = super.settings ++ Seq(
    organization := "de.pokerno"
    ,scalaVersion := "2.10.3"
    //,exportJars := true,
    //testOptions in Test += Tests.Argument("-oF"),
    ,javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
    //,scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-language:postfixOps")
    //,resolvers += "spray repo" at "http://repo.spray.io"
    //,resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
  
  lazy val deps = Seq(
    "org.scalaz" %% "scalaz-core" % "7.0.3",
    "org.slf4j" % "slf4j-simple" % "1.7.5",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    //"com.twitter" % "ostrich" % "2.3.0"
    "com.twitter" %% "util-core" % "6.10.0",
    "commons-codec" % "commons-codec" % "1.9"
  )
  
  lazy val testDeps = Seq(
    "org.scalatest" %% "scalatest" % "1.9.2" % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  )

  lazy val protocol = Project(
    id = "pokerno-protocol",
    base = file("pokerno-protocol"),
    settings = Project.defaultSettings ++ ScroogeSBT.newSettings ++ Seq(
      name := "pokerno-protocol",
      version := "0.0.1",
      libraryDependencies ++= Seq(
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.3.3",
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.3.3",
        "com.twitter" %% "scrooge-core" % "3.15.0",
        "org.apache.thrift" % "libthrift" % "0.9.1"
      )
    ) ++ assemblySettings 
  ).settings(
    ScroogeSBT.scroogeBuildOptions in Compile := Seq(),
    ScroogeSBT.scroogeThriftOutputFolder in Compile <<= (sourceDirectory) { _ / "main/scala" }
  )
  
  lazy val engine = Project(
    id = "pokerno-engine",
    base = file("pokerno-engine"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-engine",
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
      libraryDependencies ++= deps ++ testDeps ++ Seq(
        "io.netty" % "netty-all" % nettyVersion
      )
    ) ++ assemblySettings
  )

  lazy val stompGateway = Project(
    id = "pokerno-gateway-stomp",
    base = file("pokerno-gateway-stomp"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-gateway-stomp",
      version := "0.0.1",
      libraryDependencies ++= deps ++ testDeps ++ Seq(
        "asia.stampy" % "stampy-core" % "1.0-RELEASE",
        //"asia.stampy" % "stampy-NETTY-client-server-RI" % "1.0-RELEASE",
        "io.netty" % "netty-all" % nettyVersion
      )
    ) ++ assemblySettings
  )

  lazy val server = Project(
    id = "pokerno-server",
    base = file("pokerno-server"),
    settings = Project.defaultSettings ++ gitVersion ++ Seq(
      name := "pokerno-server",
      //version := "0.0.1",
      libraryDependencies ++= testDeps ++ Seq(
        "com.codahale.metrics" % "metrics-core" % "3.0.1",
        "redis.clients" % "jedis" % "2.2.1",
        "org.slf4j" % "slf4j-simple" % "1.7.5",
        "com.github.scopt" %% "scopt" % scoptVersion
      )
    ) ++ assemblySettings ++ Seq(
      assemblyOption in assembly ~= { _.copy(includeScala = false, includeDependency = false) }
    )
  ) dependsOn(engine, protocol, httpGateway, stompGateway)

  lazy val ai = Project(
    id = "pokerno-ai",
    base = file("pokerno-ai"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-ai",
      version := "0.0.2",
      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % scoptVersion
      ) ++ testDeps
    ) ++ assemblySettings
  ) dependsOn(engine, server)

  lazy val util = Project(
    id = "pokerno-util",
    base = file("pokerno-util"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-util",
      version := "0.0.1",
      libraryDependencies ++= testDeps
    ) ++ assemblySettings
  ) dependsOn(engine)
  
  lazy val replay = Project(
    id = "pokerno-replay",
    base = file("pokerno-replay"),
    settings = Project.defaultSettings ++ gitVersion ++ Seq(
      name := "pokerno-replay",
      libraryDependencies ++= Seq(
        "jline" % "jline" % "2.11",
        "com.github.scopt" %% "scopt" % scoptVersion
      )
    ) ++ assemblySettings ++ Seq(
      assemblyOption in assembly ~= { _.copy(includeScala = false, includeDependency = false) }
    )
  ) dependsOn(util, engine, server)
  
  lazy val bench = Project(
    id = "pokerno-bench",
    base = file("pokerno-bench"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-bench",
      version := "0.0.1"
    ) ++ assemblySettings
  ) dependsOn(engine)
  
  lazy val cli = Project(
    id = "pokerno-cli",
    base = file("pokerno-cli"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-cli",
      version := "0.0.1",
      libraryDependencies ++= Seq(
        "org.scala-sbt" % "command" % "0.12.4"
      )
    ) ++ assemblySettings
  ) dependsOn(engine)
  
  lazy val console = Project(
    id = "pokerno-console",
    base = file("pokerno-console"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-console",
      version := "0.0.1",
      libraryDependencies ++= Seq(
        "jline" % "jline" % "2.11",
        "com.github.scopt" %% "scopt" % scoptVersion
      ) ++ testDeps
    ) ++ assemblySettings
  ) dependsOn(engine, server)
  
}
