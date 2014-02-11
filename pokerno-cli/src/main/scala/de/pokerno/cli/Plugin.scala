package de.pokerno.cli

import sbt._
import Keys._
import complete.DefaultParsers._

object Cli extends Plugin {
  commands ++= Seq(nodeCommand, roomCommand, playerCommand)
  
  lazy val host = AttributeKey[String]("node rpc address")
  lazy val roomId = AttributeKey[String]("room id")
  lazy val playerId = AttributeKey[String]("player id")
  
  private val nodeCommandParser = (Space ~> StringBasic)
  lazy val nodeCommand =
    Command("node")(_ => nodeCommandParser) { (state: State, args) =>
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
      state
    }

  private val playerCommandParser = (Space ~> StringBasic ~> "where")
  lazy val playerCommand =
    Command("player")(_ => playerCommandParser) { (state: State, args) =>
      state
    }
}
