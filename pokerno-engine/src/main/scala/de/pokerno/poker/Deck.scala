package de.pokerno.poker

import util.Random

object Deck {
  final val FullBoardSize = 5

  def shuffle(cards: Cards = All) = Random.shuffle(cards)
  def apply() = shuffle()

  case class NoCardsLeft() extends Exception("No cards left in deck")
}

class Deck(private var _cards: Cards = Deck.shuffle()) {
  private var _discarded: Cards = List.empty
  private var _dealt: Cards = List.empty
  private var _burned: Cards = List.empty

  def cards = _cards
  def deal(n: Int): Cards = {
    val cards = _cards take n
    _cards = _cards diff cards
    cards
  }

  def reshuffle() {
    _cards = Random shuffle (_cards ++ _burned)
    _burned = List.empty
  }

  def share(n: Int): Cards = {
    val cards = deal(n)
    _dealt ++= cards
    cards
  }

  def burn(n: Int) {
    _burned ++= deal(n)
  }

  def burn(cards: Cards) {
    _cards = _cards diff cards
    _burned ++= cards
  }

  def without(cards: Cards) = new Deck(_cards diff cards)

  @throws[Deck.NoCardsLeft]
  def discard(old: Cards): Cards = {
    val n = old.size

    if (n > _cards.size + _burned.size) throw Deck.NoCardsLeft()

    if (n > _cards.size)
      reshuffle()

    val cards = deal(n)
    _burned ++= old
    _dealt = _dealt diff old
    cards
  }
  
  def replace(old: Cards): Map[Card, Card] = {
    val cards = discard(old)
    old.zipWithIndex.map { case (card, i) => (card, cards(i)) }.toMap
  }
}
