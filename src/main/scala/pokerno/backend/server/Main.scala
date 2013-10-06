package pokerno.backend.server

import pokerno.backend.engine._
import pokerno.backend.model._

import akka.actor.{ ActorSystem, Props }

object Main {
  val system = ActorSystem("MySystem")

  def main(args: Array[String]) {
    val dealer = new Dealer
    val broadcast = new Broadcast
    val table = new Table(6)
    
    table.addPlayer(new Player("A"), 0, 10000)
    table.addPlayer(new Player("B"), 1, 10000)
    table.addPlayer(new Player("C"), 2, 10000)
    table.addPlayer(new Player("D"), 3, 10000)
    table.addPlayer(new Player("E"), 4, 10000)
    table.addPlayer(new Player("F"), 5, 10000)
    
    val stake = new Stake(50.0)
    
    val game = new Game(Game.Texas, Some(Game.NoLimit), Some(9))
    val gameplay = new Gameplay(dealer, broadcast, game, stake, table)

    val deal = system actorOf (Props(classOf[DealActor], gameplay), name = "test-deal-1")
    deal ! Deal.Start
  }

  def main2(args: Array[String]) {
    val instance = system actorOf (Props[Instance], name = "greeter")
    instance ! Instance.Start
  }
}
