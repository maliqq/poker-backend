package pokerno.backend.model

import pokerno.backend.poker.{Card, Deck}

object Deal {
  trait Type
  case class Hole extends Type
  case class Door extends Type
  case class Board extends Type
  
  trait Value {
    var cardsNum: Option[Int] = None
    def apply(n: Int): Value = {
      cardsNum = Some(n)
      this
    }
  }
  case object Hole extends Value
  case object Door extends Value
  case object Board extends Value
}

class Dealer(private var _deck: Deck = new Deck) {
  private var _board: List[Card] = List.empty
  def board = _board
  
  private var _pockets: Map[Player, List[Tuple2[Deal.Value, Card]]] = Map.empty
  
  def dealPocket(t: Deal.Value, n: Int, p: Player): List[Card] = {
    val cards = _deck.share(n)
    val pocket = _pockets.getOrElse(p, List.empty)
    _pockets += (p -> (pocket ++ cards.map(card => (t, card))))
    cards
  }
  
  def discard(old: List[Card], p: Player): List[Card] = {
    val cards = _deck.discard(old) // FIXME validate old
    _pockets += (p -> cards.map(card => (Deal.Hole, card)))
    cards
  }
  
  def dealBoard(n: Int): List[Card] = {
    _deck.burn(1)
    val cards = _deck.deal(n)
    _board ++= cards
    cards
  }
  
}
