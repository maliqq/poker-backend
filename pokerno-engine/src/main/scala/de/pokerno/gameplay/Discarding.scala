package de.pokerno.gameplay

import de.pokerno.poker.Cards
import de.pokerno.model.Player
import de.pokerno.model.table.seat.Sitting

private[gameplay] trait Discarding {

  val gameplay: Context
  
  import gameplay._
  
  def round = discardingRound
  
  def require(seat: Sitting) {
    round.requireDiscard(seat)
    events broadcast Events.requireDiscard(seat)
  }
  
  def discardCards(seat: Sitting, cards: Cards) {
    val newCards = round.discard(seat, cards)
    // TODO play.action(...)
    events.publish(Events.discardCards(seat, newCards)) { _.only(seat.player) }
    events.publish(Events.discardCardsNum(seat, newCards.size)) { _.except(seat.player) }
  }
  
  def standPat(seat: Sitting) {
    events broadcast Events.discardCardsNum(seat, 0)
  }
  
  def complete() {
    table.sitting foreach { seat =>
      if (seat.inPot) seat.playing()
    }
  }
  
}

object Discarding {
  
  case object Start
  
  case class Discard(player: Player, cards: Cards)
  
}
