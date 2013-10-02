package pokerno.backend.server

import pokerno.backend.engine._
import pokerno.backend.model._

import akka.actor.{ActorSystem, Props}

object Main {
  val system = ActorSystem("MySystem")

  def main(args: Array[String]) {
    val dealer = new Dealer()
    val broadcast = new Broadcast()
    val table = new Table(9)
    val stake = new Stake(10.0)
    val game = new Game(Game.Texas, Some(Game.NoLimit), Some(9))
    val gameplay = new Gameplay(dealer, broadcast, game, stake, table)
    
    val deal = system.actorOf(Props(classOf[Deal.Process], gameplay), name = "test-deal-1")
    deal ! Deal.Start
  }
  
  def main2(args: Array[String]) {
    val instance = system.actorOf(Props[Instance], name = "greeter")
    instance ! Instance.Start
  }
}
