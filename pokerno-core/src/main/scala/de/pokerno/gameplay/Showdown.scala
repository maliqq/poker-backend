package de.pokerno.gameplay

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.poker._
import de.pokerno.protocol.{ msg ⇒ message }

/*
 * Стадия вскрытия карт
 */
private[gameplay] trait Showdown {

  g: ContextLike ⇒

  import de.pokerno.util.ConsoleUtils._

  // FIXME: equal hands
  private def best(pot: SidePot, hands: Map[Player, Hand]): Tuple2[Player, Hand] = {
    var winner: Option[Player] = None
    var best: Option[Hand] = None

    hands.filter {
      case (player, hand) ⇒
        pot.members.contains(player)
    }.toList maxBy {
      case (player, hand) ⇒
        hand
    }
  }

  private def declareExclusiveWinner(pot: Pot, box: Tuple2[Seat, Int]) = {
    val (seat, pos) = box
    pot.sidePots foreach { side ⇒
      val amount = side.total
      val winner = seat.player.get
      seat wins amount
      events.declareWinner((winner, pos), amount)
    }
  }

  private def declareWinners(pot: Pot, hi: Option[Map[Player, Hand]], lo: Option[Map[Player, Hand]]) = {
    val split: Boolean = hi.isDefined && lo.isDefined

    pot.sidePots foreach { side ⇒
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
        winners += (winnerLow.get -> total / 2.0)
        winners += (winnerHigh.get -> total / 2.0)
      } else {
        if (hi.isDefined)
          winners += (winnerHigh.get -> total)
        else
          winners += (winnerLow.get -> total)
      }

      winners foreach {
        case (winner, amount) ⇒
          val pos = 0
          val seat = new Seat // FIXME wtf?
          seat wins amount
          events.declareWinner((winner, pos), amount)
      }
    }
  }

  private def rank(player: Player, ranking: Hand.Ranking): Tuple2[List[Card], Hand] = {
    val pocket = dealer pocket player
    val board = dealer.board

    if (board.size == 0)
      return (pocket, ranking(pocket).get)

    val hands = for {
      pair ← pocket combinations 2;
      board ← dealer.board combinations 3
    } yield ranking(pair ++ board).get

    (pocket, hands.toList.max(Ranking))
  }

  private def showHands(ranking: Hand.Ranking): Map[Player, Hand] = {
    var hands: Map[Player, Hand] = Map.empty

    table.seatsAsList.zipWithIndex filter (_._1 inPot) foreach {
      case (seat, pos) ⇒
        val (pocket, hand) = rank(seat.player get, ranking)
        val player = seat.player.get
        hands += (player -> hand)
        //events.publish(message.ShowCards(pos = pos, player = player, cards = pocket))
        events.declareHand((player, pos), pocket, hand)
    }
    hands
  }

  def showdown() {
    val stillInPot = table.seatsAsList.zipWithIndex filter (_._1 inPot)
    if (stillInPot.size == 1) {
      declareExclusiveWinner(round.pot, stillInPot head)
    } else if (stillInPot.size > 1) {
      var hiHands: Option[Map[Player, Hand]] = None
      var loHands: Option[Map[Player, Hand]] = None

      game.options.hiRanking match {
        case Some(ranking) ⇒ hiHands = Some(showHands(ranking))
        case None          ⇒
      }
      game.options.loRanking match {
        case Some(ranking) ⇒ loHands = Some(showHands(ranking))
        case None          ⇒
      }
      declareWinners(round.pot, hiHands, loHands)
    }
  }
}
