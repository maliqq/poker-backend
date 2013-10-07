package pokerno.backend.cli

import pokerno.backend.model._
import pokerno.backend.engine._
import akka.actor.{ ActorSystem, Props }
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
  
  val system = ActorSystem("poker-console")

  def main(args: Array[String]) {
    parser.parse(args, Config()) map { config ⇒
      val gameplay = createGameplay(config)
      val deal = system.actorOf(Props(classOf[DealActor], gameplay), name = "deal-process")
      val play = system.actorOf(Props(classOf[Play], gameplay, deal), name = "play-process")
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
    
    val stack = 1500.0
    (1 to config.tableSize) foreach { i =>
      val player = new Player("player-%d".format(i))
      table.addPlayer(player, i - 1, stack)
      //broadcast.subscribe()
    }
    
    gameplay
  }
  
}
