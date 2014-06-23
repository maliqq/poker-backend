package de.pokerno.gameplay.stages

import de.pokerno.model._
import de.pokerno.poker._
import de.pokerno.gameplay.{Events, Stage, stg}
import scala.math.{BigDecimal => Decimal}

/*
 * Стадия вскрытия карт
 */

private[gameplay] case class Showdown(ctx: stg.Context) extends Stage {
  
  import ctx.gameplay._
  
  def apply() = {
    val inPot = table.sitting filter (_.inPot)
    if (inPot.size == 1) {
      winner(play.pot, inPot.head)
    } else {
      val hiHands = gameOptions.hiRanking.map(showHands(_))
      val loHands = gameOptions.loRanking.map(showHands(_))
      winners(play.pot, hiHands, loHands)
    }
  }
  
  private def best(pot: SidePot, hands: Map[Player, Hand]): List[Tuple2[Player, Hand]] = {
    val handsForThisPot = hands.toList.filter { case (player, hand) ⇒
      pot.members.contains(player)
    }
    val sortedHands = handsForThisPot.sortBy { _._2 }.reverse

    val bestHand = sortedHands.head
    sortedHands.takeWhile(_._2 == bestHand._2)
  }

  private def winner(pot: Pot, sitting: seat.Sitting) = pot.sidePots foreach { side ⇒
    val winner = sitting.player
    val amount = side.total
    distribute(side, Map(winner -> amount))
    sitting wins amount
    events broadcast Events.declareWinner(sitting, amount)
  }
  
  private def distribute(sidePot: SidePot, winners: Map[Player, Decimal]) {
    sidePot.members.foreach { case (member, amount) =>
      if (winners.contains(member)) {
        play.winner(member, winners(member))
      } else play.loser(member, amount)
    }
  }

  private def winners(pot: Pot, hi: Option[Map[Player, Hand]], lo: Option[Map[Player, Hand]]) = {
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

      distribute(side, winners)

      winners foreach { case (winner, amount) ⇒
        table.playerSeat(winner) map { sitting =>
          sitting wins amount
          events broadcast Events.declareWinner(sitting, amount)
        }
      }
    }
  }

  private def rank(player: Player, ranking: Hand.Ranking): Tuple2[Cards, Hand] = {
    val pocket = dealer pocket player
    val board = dealer.board

    if (board.size == 0)
      return (pocket, ranking(pocket).get) // FIXME None.get

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
      play.show(player, pocket)
      events broadcast Events.declareHand(seat, pocket, hand)
      hands + (player -> hand)
    }

}
