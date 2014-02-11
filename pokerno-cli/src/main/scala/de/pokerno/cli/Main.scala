package de.pokerno.cli

import sbt._
import complete.DefaultParsers._
import java.io.File

object Main extends xsbti.AppMain {
  val commands = Seq(nodeCommand, roomCommand, playerCommand)
  
  def run(configuration: xsbti.AppConfiguration): xsbti.MainResult =
      MainLoop.runLogged(initialState(configuration))
      
  def initialState(configuration: xsbti.AppConfiguration): State = {
    val commandDefinitions = commands ++ BasicCommands.allBasicCommands
    val commandsToRun = "room" +: "iflast shell" +: configuration.arguments.map(_.trim)
    State(
        configuration,
        commandDefinitions,
        Set.empty,
        None,
        commandsToRun,
        State.newHistory,
        AttributeMap.empty,
        initialGlobalLogging,
        State.Continue
      )
  }

  def initialGlobalLogging: GlobalLogging = GlobalLogging.initial(MainLogging.globalDefault _,  File.createTempFile("pokerno", "log"))
  
  lazy val host = AttributeKey[String]("node rpc address")
  lazy val roomId = AttributeKey[String]("room id")
  lazy val playerId = AttributeKey[String]("player id")
  
  private val nodeCommandParser = (Space ~> StringBasic)
  lazy val nodeCommand =
    Command("node")(_ => nodeCommandParser) { (state: State, args) =>
      println("node")
      state
    }
  
  private val roomCommandParser = (
      Space ~> "list" |
      Space ~> "create" ~> StringBasic |
      Space ~> StringBasic ~> "state" |
      Space ~> StringBasic ~> "table" |
      Space ~> StringBasic ~> "deal" |
      Space ~> StringBasic ~> "stat" |
      Space ~> StringBasic ~> "watch" |
      Space ~> StringBasic ~> "pause" |
      Space ~> StringBasic ~> "resume" |
      Space ~> StringBasic ~> "deal" ~> "cancel" |
      Space ~> StringBasic ~> "close"
    )
      
  lazy val roomCommand =
    Command("room")(_ => roomCommandParser) { (state: State, args) =>
      println("room")
      state
    }

  private val playerCommandParser = (Space ~> StringBasic ~> "where")
  lazy val playerCommand =
    Command("player")(_ => playerCommandParser) { (state: State, args) =>
      println("player")
      state
    }
}
