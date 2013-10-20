package pokerno.backend.ai

import pokerno.backend.model._

import akka.actor.{ActorSystem, Props}
import scala.math.{BigDecimal => Decimal}
import pokerno.backend.protocol._
import pokerno.backend.model._
import pokerno.backend.engine._

object Main {
  final val stack: Decimal = 10000
  
  val game = new Game(Game.Texas)
  val stake = new Stake(10)
  val table = new Table(game.tableSize)
  val dealer = new Dealer
  val broadcast = new EventBus
  val gameplay = new Gameplay(dealer, broadcast, game, stake, table)
  
  val system = ActorSystem("poker-cli")
  val deal = system.actorOf(Props(classOf[DealActor], gameplay), name = "deal-process")
  
  val bots = (0 to table.size - 1).map { i =>
    system.actorOf(Props(classOf[Bot], deal, i, stack, game, stake))
  }
  
  def main(args: Array[String]) {
    deal ! Deal.Done
  }
}
