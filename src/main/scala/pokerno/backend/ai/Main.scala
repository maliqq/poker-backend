package pokerno.backend.ai

import pokerno.backend.model._

import akka.actor.{ActorSystem, Props}
import scala.math.{BigDecimal => Decimal}
import pokerno.backend.protocol._
import pokerno.backend.model._
import pokerno.backend.engine._

object Main {
  final val stack: Decimal = 10000

  val system = ActorSystem("poker-cli")
  
  val game = new Game(Game.Texas)
  val stake = new Stake(10)
  
  val instance = system.actorOf(Props(classOf[Instance], game, stake), name="poker-instance")
  
  val bots = (0 to game.tableSize - 1).map { i =>
    system.actorOf(Props(classOf[Bot], instance, i, stack, game, stake))
  }
  
  def main(args: Array[String]) {
    instance ! Instance.Start
  }
}
