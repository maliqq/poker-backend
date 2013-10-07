package pokerno.backend.cli

import pokerno.backend.model._
import scala.math.{ BigDecimal => Decimal }

case class Config(
  val betSize: Decimal = .0,
  val tableSize: Int = 6,
  val mixedGame: Option[Game.Mixed] = None,
  val limitedGame: Option[Game.Limited] = None
)

object Main {
  
  val parser = new scopt.OptionParser[Config]("poker-console") {
    opt[Int]('t', "table-size") action { (value, c) => c.copy(tableSize = value) } text("Table size")
    opt[Decimal]('b', "bet-size") action { (value, c) => c.copy(betSize = value) } text("Bet size")
    opt[String]("mix") action { (value, c) => c.copy(limitedGame = Some(value)) } text("Mixed game")
    opt[String]('g', "game") action { (value, c) => c.copy(mixedGame = Some(value)) } text("Limited game")
  }
  
  def main(args: Array[String]) {
    parser.parse(args, Config()) map { config =>
      Console printf("Hello, world!")
    }
  }
  
}
