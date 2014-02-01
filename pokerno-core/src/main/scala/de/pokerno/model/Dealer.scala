package de.pokerno.model

import de.pokerno.poker.{ Card, Deck }
import de.pokerno.protocol.wire

case class DealingOptions(
    dealType: DealCards.Value,
    cardsNum: Option[Int] = None
)

object DealCards {

  abstract class Value {
    def apply(n: Int) = DealingOptions(this, Some(n))
  }
  
  case object Board extends Value
  
  case object Door extends Value
  
  case object Hole extends Value
  
}

class Dealer(private var _deck: Deck = new Deck) {
  
  private var _board: List[Card] = List.empty
  def board = _board

  private var _pockets: Map[Player, List[Card]] = Map.empty
  def pocket(p: Player): List[Card] = _pockets(p)

  def dealPocket(cards: List[Card], p: Player) = {
    val pocket = _pockets getOrElse (p, List.empty)
    _pockets += (p -> (pocket ++ cards))
    cards
  }
  
  def dealPocket(n: Int, p: Player): List[Card] = {
    val cards = _deck share n
    dealPocket(cards, p)
  }
  
  def dealBoard(cards: List[Card]) = {
    _board ++= cards
    cards
  }

  def dealBoard(n: Int): List[Card] = {
    _deck.burn(1)
    val cards = _deck deal n
    dealBoard(cards)
  }

  def discard(old: List[Card], p: Player): List[Card] = {
    val cards = _deck discard old // FIXME validate old
    _pockets += (p -> cards)
    cards
  }

}
