package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.poker.{Card, Hand}
import pokerno.backend.protocol._
import scala.math.{BigDecimal => Decimal}

class Context(
    val dealer: Dealer,
    val game: Game,
    val pot: Pot,
    val stake: Stake,
    val table: Table) {
}

trait Stage {
  def run(context: Context)
}

class StageContainer(var context: Context = null) extends Runnable {
  this : Stage =>
    def run: Unit = run(context)
}

class Betting(private var _bigBets: Boolean = false) extends Stage {
  def run(context: Context) {
  }
}


object Betting extends Stage {
  def apply(bigBets: Boolean): Stage = new Betting(bigBets)
  
  def run(context: Context) {
    (new Betting).run(context)
  }
}

object Discarding extends Stage {
  def run(context: Context) {
  }
}

class Dealing(private var _dealType: Deal.Value) extends Stage {
  def run(context: Context) {
    _dealType match {
    case Deal.Hole | Deal.Door =>
      var n: Int = _dealType.cardsNum.getOrElse(0)
      if (n == 0)
        n = context.game.options.pocketSize
      
      var seats: List[Tuple2[Int, Seat]] = List()
      seats foreach { case (pos, seat) =>
        val message = new Message.DealCards(
            _type = _dealType,
            pos = Some(pos),
            cards = context.dealer.dealPocket(_dealType, n, seat.player.get)
        )
      }
      
    case Deal.Board =>
      val message = new Message.DealCards(
          _type = _dealType,
          pos = None,
          cards = context.dealer.dealBoard(_dealType.cardsNum.get)
      )
    }
  }
}

class PostAntes extends Stage {
  def run(context: Context) {
    val seats: List[Tuple2[Int, Seat]] = List.empty
    seats foreach {case (pos, seat) =>
      val bet = Bet.force(Bet.Ante, context.stake)
    }
  }
}

class BringIn extends Stage {
  def run(context: Context) {
    
  }
}

class BettingComplete extends Stage {
  def run(context: Context) {
    val total = context.pot.total
  }
}

class Showdown extends Stage {
  def best(pot: SidePot, hands: Map[Player, Hand]): Tuple2[Option[Player], Option[Hand]] = {
    var winner: Option[Player] = None
    var best: Option[Hand] = None
    pot.members.keys foreach { member =>
      val hand: Option[Hand] = hands.get(member)
      if (hand.isDefined) {
        winner = Some(member)
        best = hand
      }
    }
    (winner, best)
  }
  
  def declareWinner(pos: Int) {
    
  }
  
  def declareWinners(hi: Option[Map[Player, Hand]], lo: Option[Map[Player, Hand]]) = {
    val split: Boolean = hi.isDefined && lo.isDefined
    val pot = new Pot
    pot.sidePots foreach { side  =>
      val total = side.total
      var winnerLow: Option[Player] = None
      var winnerHigh: Option[Player] = None
      var bestLow: Option[Hand] = None
      
      if (lo.isDefined) {
        val (_winner, _best) = best(side, lo.get)
        winnerLow = _winner
        bestLow = _best
      }
      
      if (hi.isDefined)
        winnerHigh = best(side, hi.get)._1
      
      var winners: Map[Player, Decimal] = Map.empty
      if (split && bestLow.isDefined) {
        winners += (winnerLow.get -> total / 2.)
        winners += (winnerHigh.get -> total / 2.)
      } else {
        if (hi.isDefined)
          winners += (winnerHigh.get -> total)
        else
          winners += (winnerLow.get -> total)
      }
    }
  }
  
  def showHands(r: Hand.Ranking): Option[Map[Player, Hand]] = Some(Map.empty)
  
  def run(context: Context) {
    val stillInPot: List[Tuple2[Int, Seat]] = List.empty
    if (stillInPot.size == 1) {
      declareWinner(stillInPot(0)._1)
    } else {
      var hiHands: Option[Map[Player, Hand]] = None
      var loHands: Option[Map[Player, Hand]] = None
      
      context.game.options.hiRanking match {
        case Some(ranking) => hiHands = showHands(ranking)
      }
      context.game.options.loRanking match {
        case Some(ranking) => loHands = showHands(ranking)
      }
      declareWinners(hiHands, loHands)
    }
  }
}

object Stages {
  val Default = List[Stage](
      
  )
}

object Streets {
  val ByGameGroup: Map[Game.Group, List[Street]] = Map(
      Game.Holdem -> List(
          Street.Preflop(
              List(
                 new Dealing(Deal.Hole),
                 Betting
             )
          ),
          Street.Flop(
              List(
                 new Dealing(Deal.Board(3)),
                 Betting
             )
         ),
          Street.Turn(
              List(
                 new Dealing(Deal.Board(1)),
                 Betting(bigBets = true)
             )
         ),
          Street.River(
              List(
                 new Dealing(Deal.Board(1)),
                 Betting
             )
          )
      ),
      
      Game.SevenCard -> List(
          Street.Second(
              List(
                 new Dealing(Deal.Hole(2))
             )
         ),
          Street.Third(
              List(
                 new Dealing(Deal.Door(1)),
                 Betting
             )
         ),
          Street.Fourth(
              List(
                 new Dealing(Deal.Door(1)),
                 Betting
             )
         ),
          Street.Fifth(
              List(
                 new Dealing(Deal.Door(1)),
                 Betting(bigBets = true)
             )
         ),
          Street.Sixth(
              List(
                 new Dealing(Deal.Door(1)),
                 Betting
             )
         ),
          Street.Seventh(
              List(
                 new Dealing(Deal.Hole(1)),
                 Betting
             )
         )
      ),
      
      Game.SingleDraw -> List(
          Street.Predraw(
              List(
                 new Dealing(Deal.Hole(5)),
                 Betting,
                 Discarding
             )
         ),
          Street.Draw(
              List(
                 Betting(bigBets = true),
                 Discarding
             )
         )
      ),
      
      Game.TripleDraw -> List(
          Street.Predraw(
              List(
                 new Dealing(Deal.Hole),
                 Betting,
                 Discarding
             )
         ),
          Street.FirstDraw(
              List(
                 Betting,
                 Discarding
             )
         ),
          Street.SecondDraw(
              List(
                 Betting(bigBets = true),
                 Discarding
             )
         ),
          Street.ThirdDraw(
              List(
                 Betting,
                 Discarding
             )
          )
      )
  )
}
