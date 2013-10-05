package pokerno.backend.model

import pokerno.backend.poker.{Card, Deck}

object Dealer {
  trait DealType
  
  case object Hole extends DealType
  case object Door extends DealType
  case object Board extends DealType
}

class Dealer(private var _deck: Deck = new Deck) {
  private var _board: List[Card] = List.empty
  def board = _board
  
  private var _pockets: Map[Player, List[Card]] = Map.empty
  def pocket(p: Player): List[Card] = _pockets(p)
  
  def dealPocket(t: Dealer.DealType, n: Int, p: Player): List[Card] = {
    val cards = _deck share(n)
    val pocket = _pockets getOrElse(p, List.empty)
    _pockets += (p -> (pocket ++ cards))
    cards
  }
  
  def discard(old: List[Card], p: Player): List[Card] = {
    val cards = _deck discard(old) // FIXME validate old
    _pockets += (p -> cards)
    cards
  }
  
  def dealBoard(n: Int): List[Card] = {
    _deck.burn(1)
    val cards = _deck deal(n)
    _board ++= cards
    cards
  }
  
}
