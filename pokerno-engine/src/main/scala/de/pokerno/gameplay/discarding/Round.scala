package de.pokerno.gameplay.discarding

import de.pokerno.poker.Cards
import de.pokerno.model.{Table, Game, Player, Dealer}
import de.pokerno.model.table.seat.Sitting
import de.pokerno.gameplay

private[gameplay] class Round(table: Table, game: Game, dealer: Dealer) extends gameplay.Round(table) {
  
  def requireDiscard(seat: Sitting) {
    acting = seat
  }
  
  def discard(seat: Sitting, cards: Cards) = {
    val newCards = dealer.discard(cards, seat.player)
    seat.betting()
    newCards
  }
  
}
