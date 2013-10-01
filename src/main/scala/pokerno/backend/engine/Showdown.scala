package pokerno.backend.engine

import scala.math.{BigDecimal => Decimal}
import pokerno.backend.protocol._
import pokerno.backend.model._
import pokerno.backend.poker._

trait Showdown {
context: Gameplay.Context =>
  def best(pot: SidePot, hands: Map[Player, Hand]): Tuple2[Player, Hand] = {
    var winner: Option[Player] = None
    var best: Option[Hand] = None
    
    hands.filter { case (player, hand) =>
      pot.members.contains(player)
    }.toList.maxBy { case (player, hand) =>
      hand
    }
  }
  
  def declareWinner(box: Tuple2[Seat, Int]) = {
    val (seat, pos) = box
    betting.pot.sidePots foreach { side =>
      val amount = side.total
      val winner = seat.player.get
      seat.net(amount)
      val message = Message.Winner(pos = pos, winner = winner, amount = amount)
      context.broadcast.all(message)
    }
  }
  
  def declareWinners(pot: Pot, hi: Option[Map[Player, Hand]], lo: Option[Map[Player, Hand]]) = {
    val split: Boolean = hi.isDefined && lo.isDefined
    pot.sidePots foreach { side  =>
      val total = side.total
      var winnerLow: Option[Player] = None
      var winnerHigh: Option[Player] = None
      var bestLow: Option[Hand] = None
      
      if (lo.isDefined) {
        val (_winner, _best) = best(side, lo.get)
        winnerLow = Some(_winner)
        bestLow = Some(_best)
      }
      
      if (hi.isDefined)
        winnerHigh = Some(best(side, hi.get)._1)
      
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
      
      winners foreach { case (winner, amount) =>
        val pos = 0
        val seat = new Seat
        seat.net(amount)
        val message = Message.Winner(pos = pos, winner = winner, amount = amount)
      }
    }
  }
  
  def rank(player: Player, ranking: Hand.Ranking): Tuple2[List[Card], Hand] = {
    val pocket = context.dealer.pocket(player)
    val board = context.dealer.board
    
    if (board.size == 0)
      return (pocket, ranking(pocket).get)
    
    var hands = for {
      pair <- pocket.combinations(2);
      board <- context.dealer.board.combinations(3)
    } yield(ranking(pair ++ board).get)
  
    (pocket, hands.toList.max)
  }

  def showHands(ranking: Hand.Ranking): Map[Player, Hand] = {
    var hands: Map[Player, Hand] = Map.empty
    
    context.table.where(_.inPot) foreach { case (seat, pos) =>
      val (pocket, hand) = rank(seat.player.get, ranking)
      hands += (seat.player.get -> hand)
      val message = Message.ShowHand(pos = pos, cards = pocket, hand = hand)
      context.broadcast.all(message)
    }
    hands
  }
  
  def showdown {
    val stillInPot = context.table.where(_.inPot)
    if (stillInPot.size == 1) {
      declareWinner(stillInPot.head)
    } else {
      var hiHands: Option[Map[Player, Hand]] = None
      var loHands: Option[Map[Player, Hand]] = None
      
      context.game.options.hiRanking match {
        case Some(ranking) => hiHands = Some(showHands(ranking))
        case None =>
      }
      context.game.options.loRanking match {
        case Some(ranking) => loHands = Some(showHands(ranking))
        case None =>
      }
      declareWinners(betting.pot, hiHands, loHands)
    }
  }
}
