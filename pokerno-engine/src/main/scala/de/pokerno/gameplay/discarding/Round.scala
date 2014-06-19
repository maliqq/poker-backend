package de.pokerno.gameplay.discarding

import de.pokerno.poker.Cards
import de.pokerno.model.{Table, Game, Player, Dealer, seat}
import de.pokerno.gameplay

private[gameplay] class Round(table: Table, game: Game, dealer: Dealer) extends gameplay.Round(table) {
  
  def requireDiscard(sitting: seat.Sitting) {
    acting = sitting
  }
  
  def discard(sitting: seat.Sitting, cards: Cards) = {
    val newCards = dealer.discard(cards, sitting.player)
    sitting.betting()
    newCards
  }
  
}
