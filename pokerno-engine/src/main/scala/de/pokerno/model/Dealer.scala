package de.pokerno.model

import de.pokerno.poker.{ Cards, Card, Deck }
import com.fasterxml.jackson.annotation.JsonProperty

class Dealer(private var _deck: Deck = new Deck) {

  private var _board: Cards = Cards.empty
  
  def board = _board
  @JsonProperty("board") def boardAsBytes: Array[Byte] = _board

  private var _pockets: Map[Player, Cards] = Map.empty
  
  def pocket(p: Player): Cards = _pockets(p)
  def pocketOption(p: Player): Option[Cards] = _pockets.get(p)

  def dealPocket(cards: Cards, p: Player): Cards = {
    _deck.burn(cards)
    val pocket = _pockets getOrElse (p, List.empty)
    _pockets += (p -> (pocket ++ cards))
    cards
  }

  def dealPocket(n: Int, p: Player): Cards = {
    val cards = _deck share n
    dealPocket(cards, p)
  }

  def dealBoard(cards: Cards): Cards = {
    _deck.burn(cards)
    _board ++= cards
    cards
  }

  def dealBoard(n: Int): Cards = {
    _deck.burn(1)
    val cards = _deck deal n
    dealBoard(cards)
  }

  def discard(old: Cards, p: Player): Cards = {
    val cards = _deck discard old // FIXME validate old
    _pockets += (p -> cards)
    cards
  }

}
