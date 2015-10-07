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
        "io.netty" % "netty-all" % nettyVersion
      )
    ) ++ assemblySettings
  ) dependsOn(engine)

  lazy val data = Project(
    id = "pokerno-data",
    base = file("pokerno-data"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-data",
      libraryDependencies ++= testDeps ++ Seq(
        //"com.typesafe.slick" %% "slick" % "2.0.2",
        //"org.slf4j" % "slf4j-nop" % "1.6.4",
        "postgresql" % "postgresql" % "9.1-901.jdbc4",
        "org.squeryl" %% "squeryl" % "0.9.5-6",
        "org.mongodb" %% "casbah" % "2.7.2",
        "com.datastax.cassandra" % "cassandra-driver-core" % "2.0.2"
        //"org.apache.cassandra" % "cassandra-thrift" % "2.0.8",
        //"com.netflix.astyanax" % "astyanax" % "1.56.48",
      )
    )
  ) dependsOn(engine)

  lazy val payment = Project(
    id = "pokerno-payment",
    base = file("pokerno-payment"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-payment",
      libraryDependencies ++= testDeps
    )
  ) dependsOn(protocol, data)

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
        "com.github.scopt" %% "scopt" % scoptVersion
      )
    ) ++ assemblySettings ++ Seq(
      assemblyOption in assembly ~= { _.copy(includeScala = false, includeDependency = false) }
    )
  ) dependsOn(engine, payment, data, protocol, httpGateway)

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

  lazy val replay = Project(
    id = "pokerno-replay",
    base = file("pokerno-replay"),
    settings = Project.defaultSettings ++ Seq(
      name := "pokerno-replay",
      libraryDependencies ++= Seq(
        "jline" % "jline" % "2.11",
        "com.github.scopt" %% "scopt" % scoptVersion
      )
    ) ++ assemblySettings ++ Seq(
      assemblyOption in assembly ~= { _.copy(includeScala = false, includeDependency = false) }
    )
  ) dependsOn(engine, server)

}
