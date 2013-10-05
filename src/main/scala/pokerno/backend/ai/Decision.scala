package pokerno.backend.ai

import pokerno.backend.model._
import pokerno.backend.poker._
import pokerno.backend.poker.Math._
import scala.math.{BigDecimal => Decimal}

case class Decision(
  val minBet: Decimal = .0,
  val maxBet: Decimal = .0,
  val raiseChance: Double = .0,
  val allInChance: Double = .0
)

trait Simple {
  def stake: Stake
  def stack: Decimal
  def bet: Decimal
  
  def decidePreflop(cards: List[Card]): Decision = {
    val group = Tables sklanskyMalmuthGroup(cards.head, cards.last)
    val bb = stake.bigBlind
  
    Console printf("group=%d", group)
    
    group match {
    case 9 => Decision(maxBet = .0)
  
    case 7 | 8 => Decision(maxBet = bb)
  
    case 5 | 6 => Decision(
        minBet = bb,
        maxBet = bb * 4,
        raiseChance = .2
      )
  
    case 3 | 4 => Decision(
        maxBet = stack + bet,
        raiseChance = .5,
        allInChance = .1
      )
  
    case 1 | 2 => Decision(
        maxBet = stack + bet,
        raiseChance = .5,
        allInChance = .1
      )
    }
  }
  
  def opponentsNum: Int
  def decideBoard(cards: List[Card], board: List[Card]): Decision = {
    val chances = Against(opponentsNum) withBoard(cards, board)
  
    Console.printf("chances=%s", chances)
  
    val tightness = 0.7
    if (chances.wins > tightness)
      Decision(
        maxBet = stack + bet,
        raiseChance = .5,
        allInChance = .5
      )
    else if (chances.wins > tightness / 2)
      Decision(
        maxBet = (stack + bet) / 3.0,
        raiseChance = .2
      )
    else if (chances.ties > 0.8)
      Decision(
        maxBet = stack + bet,
        raiseChance = .0,
        allInChance = .0
      )
    else
      Decision()
  }
}
