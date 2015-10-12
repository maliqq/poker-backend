package de.pokerno.model

import de.pokerno.poker.{ Cards, Card, Deck }
import com.fasterxml.jackson.annotation.JsonProperty

class Dealer(private val _deck: Deck = new Deck) {

  private val _deckCards: Cards = _deck.cards
  def deck = _deckCards

  private var _board: Cards = Cards.empty
  def board = _board

  private var _pockets = collection.mutable.Map.empty[Player, Cards]

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

  def discard(cards: Cards, p: Player): Cards = {
    val pocketCards = pocket(p)
    val replace = _deck replace pocketCards.filter(cards.contains(_))
    val newCards = pocketCards.map { card =>
      if (replace.contains(card)) replace(card)
      else card
    }
    _pockets(p) = newCards
    newCards
  }

}
