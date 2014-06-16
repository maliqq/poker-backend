package de.pokerno.gameplay

import de.pokerno.poker.Cards
import de.pokerno.model.{Player, seat}

trait Discarding {

  val gameplay: Context
  
  import gameplay._
  
  def round = discardingRound
  
  def require(sitting: seat.Sitting) {
    round.requireDiscard(sitting)
    events broadcast Events.requireDiscard(sitting)
  }
  
  def discardCards(sitting: seat.Sitting, cards: Cards) {
    val newCards = round.discard(sitting, cards)
    // TODO play.action(...)
    events.publish(Events.discardCards(sitting, newCards)) { _.only(sitting.player) }
    events.publish(Events.discardCardsNum(sitting, newCards.size)) { _.except(sitting.player) }
  }
  
  def standPat(sitting: seat.Sitting) {
    events broadcast Events.discardCardsNum(sitting, 0)
  }
  
  def complete() {
    table.discardingComplete()
  }
  
}

object Discarding {
  
  case object Start
  
  case class Discard(player: Player, cards: Cards)
  
}
