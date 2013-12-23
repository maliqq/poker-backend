package de.pokerno.cli

import de.pokerno.model._
import de.pokerno.gameplay._
import akka.actor.{ ActorRef, ActorSystem, Props }
import scala.math.{ BigDecimal ⇒ Decimal }

case class Config(
  val betSize: Decimal = 100.0,
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
      val instance = system.actorOf(Props(classOf[Instance], gameplay), name = "deal-process")
      val play = system.actorOf(Props(classOf[Play], gameplay, instance, config.tableSize), name = "play-process")
      instance ! Instance.Start
    }
  }

  def createGameplay(config: Config): Gameplay = {
    val broadcast = new EventBus
    val variation = if (config.mixedGame.isDefined)
      new Mix(config.mixedGame.get, config.tableSize)
    else
      new Game(config.limitedGame, Some(Game.NoLimit), Some(config.tableSize))
    val table = new Table(config.tableSize)
    val stake = new Stake(config.betSize, Ante = Right(true))
    val gameplay = new Gameplay(broadcast, variation, stake, table)

    gameplay
  }

}
