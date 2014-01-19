package de.pokerno.model

import de.pokerno.poker.{ Card, Deck }
import de.pokerno.protocol.wire

object DealCards {
  type Value = wire.DealType
  final val Board = wire.DealType.BOARD
  final val Door = wire.DealType.DOOR
  final val Hole = wire.DealType.HOLE
}

class Dealer(private var _deck: Deck = new Deck) {
  
  private var _board: List[Card] = List.empty
  def board = _board

  private var _pockets: Map[Player, List[Card]] = Map.empty
  def pocket(p: Player): List[Card] = _pockets(p)

  def dealPocket(cards: List[Card], p: Player) {
    val pocket = _pockets getOrElse (p, List.empty)
    _pockets += (p -> (pocket ++ cards))
  }
  
  def dealPocket(n: Int, p: Player): List[Card] = {
    val cards = _deck share (n)
    dealPocket(cards, p)
    cards
  }
  
  def dealBoard(cards: List[Card]) {
    _board ++= cards
  }

  def dealBoard(n: Int): List[Card] = {
    _deck.burn(1)
    val cards = _deck deal (n)
    dealBoard(cards)
    cards
  }

  def discard(old: List[Card], p: Player): List[Card] = {
    val cards = _deck discard (old) // FIXME validate old
    _pockets += (p -> cards)
    cards
  }

}
