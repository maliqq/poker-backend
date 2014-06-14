package de.pokerno.gameplay

import de.pokerno.poker.Cards
import de.pokerno.model.{Player, seat}

trait Discarding {

  val gameplay: Context
  
  import gameplay._
  
  def round = discardingRound
  
  def requireDiscard() {
    
  }
  
  def discardCards(sitting: seat.Sitting, cards: Cards) {
    val newCards = round.discard(sitting, cards)
    // TODO play.action(...)
    events.publish(Events.discardCards(sitting, cards)) { _.only(sitting.player) }
    events broadcast Events.discardCardsNum(sitting, cards.size)
  }
  
  def standPat(sitting: seat.Sitting) {
    events broadcast Events.discardCardsNum(sitting, 0)
  }
  
}

object Discarding {
  
  case object Start
  
  case class Discard(player: Player, cards: Cards)
  
}
