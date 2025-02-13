import sbt._
import Keys._
import Process._
// sbt-assembly
import sbtassembly.Plugin._
import AssemblyKeys._
//import com.twitter.scrooge.ScroogeSBT

object PokernoBuild extends Build {
  val pokernoVersion  = "0.0.1"
  val scoptVersion    = "3.1.0"
  val nettyVersion    = "4.0.19.Final"
  val akkaVersion     = "2.2.3"
  val finagleVersion  = "6.16.0"

  override lazy val settings = super.settings ++ Seq(
    organization := "de.pokerno",
    scalaVersion := "2.10.3",
    //exportJars := true,
    //testOptions in Test += Tests.Argument("-oF"),
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    //scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-language:postfixOps"),
    resolvers += "spray repo" at "http://repo.spray.io"//,
    //resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )

  lazy val deps = Seq(
    "org.scalaz" %% "scalaz-core" % "7.0.3",
    "org.slf4j" % "slf4j-simple" % "1.7.5",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    //"com.twitter" % "ostrich" % "2.3.0"
    "org.scala-lang" % "scala-reflect" % "2.10.3",
    "com.twitter" %% "util-core" % "6.10.0",
    "commons-codec" % "commons-codec" % "1.9",
    "com.codahale.metrics" % "metrics-core" % "3.0.1"
  )

  lazy val testDeps = Seq(
    "org.scalatest" %% "scalatest" % "2.2.0" % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  )

  lazy val protocol = Project(
    id = "pokerno-protocol",
    base = file("pokerno-protocol"),
    settings = Project.defaultSettings ++ /*ScroogeSBT.newSettings ++*/ Seq(
      name := "pokerno-protocol",
      version := "0.0.1",
      unmanagedSourceDirectories in Compile += baseDirectory.value / "target/generated-sources/thrift/scrooge",
      libraryDependencies ++= Seq(
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.3.3",
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.3.3",
        "com.twitter" %% "scrooge-core" % "3.15.0",
        "org.apache.thrift" % "libthrift" % "0.9.1",
        "com.twitter" %% "finagle-core" % finagleVersion,
        "com.twitter" %% "finagle-thrift" % finagleVersion
      )
    ) ++ assemblySettings
  )
  // .settings(
  //   ScroogeSBT.scroogeBuildOptions in Compile := Seq("--finagle", "-s"),
  //   ScroogeSBT.scroogeThriftOutputFolder in Compile <<= (sourceDirectory) { _ / "main/scala" }
  // )

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
        "io.netty" % "netty-all" % nettyVersion,
        "org.bitbucket.b_c" % "jose4j" % "0.4.4"
      )
    ) ++ assemblySettings
  ) dependsOn(engine)

  lazy val server = Project(
    id = "pokerno-server",
    base = file("pokerno-server"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-server",
      //version := "0.0.1",
      libraryDependencies ++= testDeps ++ Seq(
        //"com.codahale.metrics" % "metrics-graphite" % "3.0.1",
        "io.spray" % "spray-can" % "1.2.1",
        "io.spray" % "spray-routing" % "1.2.1",
        "org.zeromq" % "jzmq" % "3.0.1",
        "redis.clients" % "jedis" % "2.2.1",
        "org.apache.kafka" %% "kafka" % "0.8.1.1" excludeAll (
            ExclusionRule(organization = "com.sun.jdmk"),
            ExclusionRule(organization = "com.sun.jmx"),
            ExclusionRule(organization = "javax.jms")
          ),
        "org.slf4j" % "slf4j-simple" % "1.7.5",
        "org.apache.httpcomponents" % "httpclient" % "4.5.1",

        "com.github.scopt" %% "scopt" % scoptVersion
      )
    ) ++ assemblySettings ++ Seq(
      assemblyOption in assembly ~= { _.copy(includeScala = false, includeDependency = false) }
    )
  ) dependsOn(engine, protocol, httpGateway)

}
