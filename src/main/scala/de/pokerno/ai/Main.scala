package de.pokerno.ai

import de.pokerno.backend._
import akka.actor.{ ActorSystem, Props }
import scala.math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.gameplay._

object Main {
  final val stack: Decimal = 10000

  val system = ActorSystem("poker-cli")

  val game = new Game(Game.Texas, Some(Game.FixedLimit), Some(9))
  val stake = new Stake(10)

  val instance = system.actorOf(Props(classOf[Instance], game, stake), name = "poker-instance")

  val bots = (0 to game.tableSize - 1).map { i ⇒
    system.actorOf(Props(classOf[Bot], instance, i, stack, game, stake))
  }

  def main(args: Array[String]) {
    val htmlEventSource = system.actorOf(Props(classOf[gateway.EventSource.Server]))
    instance ! Instance.Subscribe(htmlEventSource, "html-event-source")
    instance ! Instance.Start
  }
}
