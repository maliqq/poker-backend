package de.pokerno.model

import de.pokerno.poker.{ Cards, Card, Deck }
import com.fasterxml.jackson.annotation.JsonProperty

class Dealer(private val _deck: Deck = new Deck) {

  private var _board: Cards = Cards.empty
  def board = _board

  private var _pockets: collection.mutable.Map[Player, Cards] = collection.mutable.Map.empty
  
  def pocket(p: Player): Cards = _pockets.getOrElse(p, Cards.empty)

  def dealtPocket(cards: Cards, p: Player): Cards = {
    _pockets(p) = pocket(p) ++ cards
    cards
  }
  
  def dealPocket(n: Int, p: Player): Cards = {
    val cards = _deck share n
    _deck.burn(cards)
    dealtPocket(cards, p)
  }
  
  def dealtBoard(cards: Cards) = {
    _deck.burn(cards)
    _board ++= cards
    cards
  }

  def dealBoard(n: Int): Cards = {
    _deck.burn(1)
    val cards = _deck deal n
    dealtBoard(cards)
  }

  def discard(old: Cards, p: Player): Cards = {
    val replace = pocket(p).toSet & old.toSet
    val cards = _deck discard replace.toList
    _pockets(p) = cards
    cards
  }

}
