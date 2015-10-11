package de.pokerno.gameplay.stage.impl

import de.pokerno.model._
import de.pokerno.model.seat.impl._
import de.pokerno.poker._
import de.pokerno.gameplay.{Events, Stage}
import scala.math.{BigDecimal => Decimal}

/*
 * Стадия вскрытия карт
 */

private[gameplay] trait ShowdownStrategy {
  
  def best(pot: SidePot, hands: Map[Player, Hand]): List[Tuple2[Player, Hand]] = {
    val handsForThisPot = hands.toList.filter { case (player, hand) ⇒
      pot.members.contains(player)
    }
    val sortedHands = handsForThisPot.sortBy { _._2 }.reverse
    
    println(sortedHands)

    val bestHand = sortedHands.head
    sortedHands.takeWhile(_._2 == bestHand._2)
  }
  
  def rank(pocketCards: Cards, boardCards: Cards, ranking: Hand.Ranking): Hand = {
    if (boardCards.size == 0)
      return ranking(pocketCards).get // FIXME None.get

    val hands: List[Option[Hand]] = if (pocketCards.size > 2) {
      val _hands = for {
        pair ← pocketCards combinations 2;
        board ← boardCards combinations 3
      } yield ranking(pair ++ board)
      _hands.toList
    } else List(ranking(pocketCards ++ boardCards))

    hands.flatten.max(Ranking)
  }

  
}

private[gameplay] case class Showdown(ctx: Stage.Context) extends Stage with ShowdownStrategy {
  
  import ctx.gameplay._
  import play.dealer
  
  def apply() = {
    val inPot = table.sitting filter (_.inPot)
    val pot = play.pot

    // return uncalled bet
    pot.sidePots.headOption map { sidePot =>
      sidePot.uncalled() map { case (player, amount) =>
        sidePot.members(player) -= amount
        play.uncalled = amount
        table(player).map { won(_, amount, uncalled = true) }
      }
    }

    if (inPot.size == 1) {
      // one wins whole pot
      winner(pot, inPot.head)
    } else {
      // distribute pot between winners
      val hiHands = gameOptions.hiRanking.map(showHands(_))
      val loHands = gameOptions.loRanking.map(showHands(_))
      winners(pot, hiHands, loHands)
    }
  }
  
  private def winner(pot: Pot, seat: Sitting) = pot.sidePots foreach { side ⇒
    val winner = seat.player
    val amount = side.total
    distribute(side, Map(winner -> amount))
    won(seat, amount)
  }
  
  private def distribute(sidePot: SidePot, winners: Map[Player, Decimal]) {
    sidePot.members.foreach { case (member, amount) =>
      if (winners.contains(member)) {
        play.winner(member, winners(member) - amount)
      } else play.loser(member, amount)
    }
  }

  private def won(seat: Sitting, amount: Decimal, uncalled: Boolean = false) {
    seat wins amount
    events broadcast Events.declareWinner(seat, amount, uncalled = if (uncalled) Some(true) else None)
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
        table(winner) map { won(_, amount) }
      }
    }
  }
  
  private def showHands(ranking: Hand.Ranking): Map[Player, Hand] =
    table.sitting.filter(_.inPot).foldLeft(Map[Player, Hand]()) { case (hands, seat) =>
      val player = seat.player
      val pocket = dealer.pocket(player)
      val hand = rank(pocket, dealer.board, ranking)
      
      //events.publish(message.ShowCards(pos = pos, player = player, cards = pocket))
      play.show(player, pocket)
      events broadcast Events.declareHand(seat, pocket, hand)
      hands + (player -> hand)
    }

}
