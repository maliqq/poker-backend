package de.pokerno.model

import de.pokerno.poker.{ Card, Deck }

class Dealer(private var _deck: Deck = new Deck) {

  private var _board: Seq[Card] = Seq.empty
  def board = _board

  private var _pockets: Map[Player, List[Card]] = Map.empty
  def pocket(p: Player): Seq[Card] = _pockets(p)
  def pocketOption(p: Player): Option[Seq[Card]] = _pockets.get(p)

  def dealPocket(cards: Seq[Card], p: Player) = {
    _deck.burn(cards)
    val pocket = _pockets getOrElse (p, List.empty)
    _pockets += (p -> (pocket ++ cards))
    cards
  }

  def dealPocket(n: Int, p: Player): Seq[Card] = {
    val cards = _deck share n
    dealPocket(cards, p)
  }

  def dealBoard(cards: Seq[Card]) = {
    _deck.burn(cards)
    _board ++= cards
    cards
  }

  def dealBoard(n: Int): Seq[Card] = {
    _deck.burn(1)
    val cards = _deck deal n
    dealBoard(cards)
  }

  def discard(old: Seq[Card], p: Player): Seq[Card] = {
    val cards = _deck discard old // FIXME validate old
    _pockets += (p -> cards)
    cards
  }

}
