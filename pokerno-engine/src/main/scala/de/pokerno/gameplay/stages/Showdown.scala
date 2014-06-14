package de.pokerno.gameplay.stages

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.poker._
import de.pokerno.gameplay.{Events, Stage, stg}
import scala.math.{BigDecimal => Decimal}

/*
 * Стадия вскрытия карт
 */

case class Showdown(ctx: stg.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = {
    val inPot = table.sitting filter (_.inPot)
    if (inPot.size == 1) {
      declareWinner(inPot.head, round.pot)
    } else {
      val hiHands = gameOptions.hiRanking.map(showHands(_))
      val loHands = gameOptions.loRanking.map(showHands(_))
      declareWinners(round.pot, hiHands, loHands)
    }
  }
  
  private def best(pot: SidePot, hands: Map[Player, Hand]): List[Tuple2[Player, Hand]] = {
    val sorted = hands.filter { case (player, hand) ⇒
      pot.members.contains(player)
    }.toList sortBy { case (player, hand) ⇒
      hand
    } reverse

    val max = sorted.head
    sorted.takeWhile(_._2 == max._2)
  }

  private def declareWinner(sitting: seat.Sitting, pot: Pot) = pot.sidePots foreach { side ⇒
    val amount = side.total
    val winner = sitting.player
    sitting wins amount
    events broadcast Events.declareWinner(sitting, amount)
  }

  private def declareWinners(pot: Pot, hi: Option[Map[Player, Hand]], lo: Option[Map[Player, Hand]]) = {
    val split: Boolean = hi.isDefined && lo.isDefined

    pot.sidePots foreach { side ⇒
      val total = side.total

      val (winnersLow: List[Player], bestLow: Option[Hand]) = lo.map { _lo =>
        val _best = best(side, _lo)
        (_best.map(_._1), _best.headOption.map(_._2))
      } getOrElse((List.empty, None))

      val winnersHigh: List[Player] = hi.map { _hi =>
        best(side, _hi).map(_._1)
      } getOrElse(List.empty)

      def splitWinners(winners: List[Player], amount: Decimal): Map[Player, Decimal] = {
        if (winners.isEmpty)
          return Map.empty // prevent DivisionByZero

        val share = amount / winners.length
        winners.foldLeft[Map[Player, Decimal]](Map.empty) { case (result, winner) ⇒
          result + (winner -> share)
        }
      }
      
      // TODO остаток от деления
      val winners: Map[Player, Decimal] = if (split && bestLow.isDefined) {
        splitWinners(winnersLow, total / 2.0) ++ splitWinners(winnersHigh, total / 2.0)
      } else {
        splitWinners(
          if (hi.isDefined) winnersHigh
          else winnersLow,
          total)
      }

      winners foreach { case (winner, amount) ⇒
        table.playerSeat(winner) map { seat =>
          seat wins amount
          events broadcast Events.declareWinner(seat, amount)
        }
      }
    }
  }

  private def rank(player: Player, ranking: Hand.Ranking): Tuple2[Cards, Hand] = {
    val pocket = dealer pocket player
    val board = dealer.board

    if (board.size == 0)
      return (pocket, ranking(pocket).get)

    val hands: List[Option[Hand]] = if (pocket.size > 2) {
      val _hands = for {
        pair ← pocket combinations 2;
        board ← dealer.board combinations 3
      } yield ranking(pair ++ board)
      _hands.toList
    } else List(ranking(pocket ++ board))

    (pocket, hands.flatten.max(Ranking))
  }

  private def showHands(ranking: Hand.Ranking): Map[Player, Hand] =
    table.sitting.filter(_.inPot).foldLeft(Map[Player, Hand]()) { case (hands, seat) =>
      val player = seat.player
      val (pocket, hand) = rank(seat.player, ranking)
      
      //events.publish(message.ShowCards(pos = pos, player = player, cards = pocket))
      events broadcast Events.declareHand(seat, pocket, hand)
      hands + (player -> hand)
    }

}
