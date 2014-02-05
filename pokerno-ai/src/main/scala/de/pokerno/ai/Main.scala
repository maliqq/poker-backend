package de.pokerno.ai

import de.pokerno.backend._
import de.pokerno.backend.gateway._
import akka.actor.{ ActorSystem, Props }
import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.gameplay._

object Main {
  final val stack: Decimal = 10000

  val system = ActorSystem("poker-ai")

  val game = new Game(Game.Texas, Some(Game.FixedLimit), Some(9))
  val stake = new Stake(10)

  val instance = system.actorOf(Props(classOf[Instance], game, stake), name = "poker-instance")

  val bots = (0 to game.tableSize - 1).map { i ⇒
    system.actorOf(Props(classOf[bot.Bot], instance, i, stack, game, stake))
  }
  
  def main(args: Array[String]) {
    val gw = system.actorOf(Props(classOf[Http.Gateway]))
    val httpServer = new http.Server(gw,
        http.Config(port = 8080, webSocket = Right(true))
    )
    httpServer.start

    instance ! Instance.Subscribe(gw, "html-event-source")
    instance ! Instance.Start
  }
}
