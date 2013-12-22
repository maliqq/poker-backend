package de.pokerno.model

import de.pokerno.poker.{ Card, Deck }
import de.pokerno.backend.{protocol => proto}

object DealCards {
  type Value = proto.DealCardsSchema.DealType
  final val Board = proto.DealCardsSchema.DealType.BOARD
  final val Door = proto.DealCardsSchema.DealType.DOOR
  final val Hole = proto.DealCardsSchema.DealType.HOLE
}

class Dealer(private var _deck: Deck = new Deck) {
  private var _board: List[Card] = List.empty
  def board = _board

  private var _pockets: Map[Player, List[Card]] = Map.empty
  def pocket(p: Player): List[Card] = _pockets(p)

  def dealPocket(n: Int, p: Player): List[Card] = {
    val cards = _deck share (n)
    val pocket = _pockets getOrElse (p, List.empty)
    _pockets += (p -> (pocket ++ cards))
    cards
  }

  def discard(old: List[Card], p: Player): List[Card] = {
    val cards = _deck discard (old) // FIXME validate old
    _pockets += (p -> cards)
    cards
  }

  def dealBoard(n: Int): List[Card] = {
    _deck.burn(1)
    val cards = _deck deal (n)
    _board ++= cards
    cards
  }

}
