package de.pokerno.gameplay.stage

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.model._
import de.pokerno.poker._
import de.pokerno.gameplay.{Events, Stage, StageContext}
import scala.math.{BigDecimal => Decimal}

/*
 * Стадия вскрытия карт
 */

private[gameplay] case class Showdown(ctx: StageContext) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = {
    val stillInPot = table.seats.zipWithIndex filter (_._1 inPot)
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
  
  // FIXME: equal hands
  private def best(pot: SidePot, hands: Map[Player, Hand]): List[Tuple2[Player, Hand]] = {
    var winner: Option[Player] = None
    var best: Option[Hand] = None

    val sorted = hands.filter {
      case (player, hand) ⇒
        pot.members.contains(player)
    }.toList sortBy {
      case (player, hand) ⇒
        hand
    } reverse

    val max = sorted.head
    sorted.takeWhile(_._2 == max._2)
  }

  private def declareExclusiveWinner(pot: Pot, box: Tuple2[Seat, Int]) = {
    val (seat, pos) = box
    pot.sidePots foreach { side ⇒
      val amount = side.total
      val winner = seat.player.get
      seat wins amount
      events.publish(
        Events.declareWinner(pos, winner, amount)) { _.all() }
    }
  }

  private def declareWinners(pot: Pot, hi: Option[Map[Player, Hand]], lo: Option[Map[Player, Hand]]) = {
    val split: Boolean = hi.isDefined && lo.isDefined

    pot.sidePots foreach { side ⇒
      val total = side.total

      var winnersLow: List[Player] = List.empty
      var winnersHigh: List[Player] = List.empty
      var bestLow: Option[Hand] = None

      if (lo.isDefined) {
        val _best = best(side, lo.get)
        winnersLow = _best.map(_._1)
        bestLow = Some(_best.head._2)
      }

      if (hi.isDefined)
        winnersHigh = best(side, hi.get).map(_._1)

      def splitWinners(winners: List[Player], amount: Decimal): Map[Player, Decimal] = {
        if (winners.isEmpty)
          return Map.empty // prevent DivisionByZero

        val share = amount / winners.length
        winners.foldLeft[Map[Player, Decimal]](Map.empty) {
          case (result, winner) ⇒
            result + (winner -> share)
        }
      }

      var winners: Map[Player, Decimal] = Map.empty

      // TODO остаток от деления
      if (split && bestLow.isDefined) {
        winners ++= splitWinners(winnersLow, total / 2.0)
        winners ++= splitWinners(winnersHigh, total / 2.0)
      } else {
        winners ++= splitWinners(
          if (hi.isDefined) winnersHigh
          else winnersLow,
          total)
      }

      winners foreach {
        case (winner, amount) ⇒
          table.playerPos(winner) map { pos =>
            val seat = table.seats(pos)
            seat wins amount
            events.publish(
              Events.declareWinner(pos, winner, amount)
            ) { _.all() }
        }
      }
    }
  }

  private def rank(player: Player, ranking: Hand.Ranking): Tuple2[Cards, Hand] = {
    val pocket = dealer pocket player
    val board = dealer.board

    if (board.size == 0)
      return (pocket, ranking(pocket).get)

    val hands = if (pocket.size > 2)
      for {
        pair ← pocket combinations 2;
        board ← dealer.board combinations 3
      } yield ranking(pair ++ board).get

    else List(ranking(pocket ++ board).get)

    (pocket, hands.toList.max(Ranking))
  }

  private def showHands(ranking: Hand.Ranking): Map[Player, Hand] = {
    var hands: Map[Player, Hand] = Map.empty

    table.seats.zipWithIndex filter (_._1 inPot) foreach {
      case (seat, pos) ⇒
        val (pocket, hand) = rank(seat.player get, ranking)
        val player = seat.player.get
        hands += (player -> hand)
        //events.publish(message.ShowCards(pos = pos, player = player, cards = pocket))
        events.publish(
          Events.declareHand(pos, player, pocket, hand)
        ) { _.all() }
    }
    hands
  }

}
