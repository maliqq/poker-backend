package de.pokerno.ai

import de.pokerno.backend._
import de.pokerno.backend.gateway._
import akka.actor.{ ActorSystem, Props }
import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.backend.server.Room

object Main {
  final val stack: Decimal = 10000
  
  case class Config(
    tableSize: Int = 9,
    botsNum: Int = 9,
    stake: Int = 10,
    stack: Int = 1000
  )
  
  var parser = new scopt.OptionParser[Config]("poker-ai") {
    opt[Int]('t', "table-size") text("Table size from 2 to 10") action { (value, c) =>
      if (value >= 2 && value <= 10) c.copy(tableSize = value)
      else c
    }
    
    opt[Int]('t', "bots-num") text("Number of bots") action { (value, c) =>
      if (value <= c.tableSize) c.copy(botsNum = value)
      else c
    }
    
    opt[Int]("stake") text("Stake BB") action { (value, c) =>
      c.copy(stake = value)
    }
    
    opt[Int]("stack") text("Stack chips") action { (value, c) =>
      c.copy(stack = value)
    }
    
    help("help") text("Help")
  }

  def main(args: Array[String]) {
    parser.parse(args, Config()) map { c =>
      val game = new Game(Game.Texas, Some(Game.FixedLimit), Some(c.tableSize))
      val stake = new Stake(c.stake)
      
      val system = ActorSystem("poker-ai")
      val room = system.actorOf(Props(classOf[Room], "1", game, stake), name = "poker-instance")
      val bots = (1 to c.botsNum).map { i ⇒
        system.actorOf(Props(classOf[bot.Bot], room, i - 1, stack, game, stake))
      }
      
      val gw = system.actorOf(Props(classOf[Http.Gateway]))
      val httpServer = new http.Server(gw,
        http.Config(port = 8080, webSocket = Right(true))
      )
      httpServer.start
  
      room ! Room.Subscribe(gw, "html-event-source")
    }
  }
}
