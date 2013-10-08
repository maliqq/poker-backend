package pokerno.backend.cli

import pokerno.backend.model._
import pokerno.backend.engine._
import pokerno.backend.protocol._
import akka.actor.{ ActorRef, ActorSystem, Props }
import scala.math.{ BigDecimal ⇒ Decimal }

case class Config(
  val betSize: Decimal = .0,
  val tableSize: Int = 6,
  val mixedGame: Option[Game.Mixed] = None,
  val limitedGame: Game.Limited = Game.Texas)

object Main {

  val parser = new scopt.OptionParser[Config]("poker-console") {
    opt[Int]('t', "table-size") action { (value, c) ⇒ c.copy(tableSize = value) } text ("Table size")
    opt[Decimal]('b', "bet-size") action { (value, c) ⇒ c.copy(betSize = value) } text ("Bet size")
    opt[String]("mix") action { (value, c) ⇒ c.copy(mixedGame = Some(value)) } text ("Mixed game")
    opt[String]('g', "game") action { (value, c) ⇒ c.copy(limitedGame = value) } text ("Limited game")
  }
  
  val config = Config()
  
  val system = ActorSystem("poker-cli")

  def main(args: Array[String]) {
    parser.parse(args, config) map { config ⇒
      val gameplay = createGameplay(config)
      val deal = system.actorOf(Props(classOf[DealActor], gameplay), name = "deal-process")
      val play = system.actorOf(Props(classOf[Play], gameplay, deal), name = "play-process")
      play ! Play.Join(config.tableSize, deal)
      deal ! Deal.Start
    }
  }
  
  def createGameplay(config: Config): Gameplay = {
    val dealer = new Dealer
    val broadcast = new Broadcast
    val variation = if (config.mixedGame.isDefined)
      new Mix(config.mixedGame.get, config.tableSize)
    else
      new Game(config.limitedGame, Some(Game.NoLimit), Some(config.tableSize))
    val table = new Table(config.tableSize)
    val stake = new Stake(config.betSize)
    val gameplay = new Gameplay(dealer, broadcast, variation, stake, table)
    
    gameplay
  }
  
}
