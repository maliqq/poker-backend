package de.pokerno.cli

import de.pokerno.model._
import de.pokerno.gameplay._
import akka.actor.{ ActorRef, ActorSystem, Props }
import math.{ BigDecimal ⇒ Decimal }

case class Config(
  betSize: Decimal = 100.0,
  tableSize: Int = 6,
  mixedGame: Option[Game.Mixed] = None,
  limitedGame: Option[Game.Limited] = Some(Game.Texas))

object Main {

  val parser = new scopt.OptionParser[Config]("poker-replay") {
    opt[Int]('t', "table-size") text "Table size" action { (value, c) ⇒
      c.copy(tableSize = value)
    }
    opt[Decimal]('b', "bet-size") text "Bet size" action { (value, c) ⇒
      c.copy(betSize = value)
    }
    opt[String]("mix") text "Mixed game" action { (value, c) ⇒
      c.copy(mixedGame = value)
    }
    opt[String]('g', "game") text "Limited game" action { (value, c) ⇒
      c.copy(limitedGame = value)
    }
  }

  val config = Config()

  val system = ActorSystem("poker-cli")

  def main(args: Array[String]) {
    parser.parse(args, config) map { config ⇒
      val variation = if (config.mixedGame.isDefined)
        new Mix(config.mixedGame.get, config.tableSize)
      else
        new Game(config.limitedGame.get, Some(Game.NoLimit), Some(config.tableSize))
      val stake = new Stake(config.betSize, Ante = Right(true))
      val cycle = system.actorOf(Props(classOf[DealCycle], variation, stake), name = "deal-process")
      val playing = system.actorOf(Props(classOf[play.Play], cycle, config.tableSize), name = "play-process")
      cycle ! Deal.Start
    }
  }

}
