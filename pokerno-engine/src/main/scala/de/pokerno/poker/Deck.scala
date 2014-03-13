package de.pokerno.poker

import util.Random

object Deck {
  final val FullBoardSize = 5

  def shuffle(cards: List[Card] = Cards) = Random.shuffle(cards)
  def apply() = shuffle()

  case class NoCardsLeft() extends Exception("No cards left in deck")
}

class Deck(private var _cards: List[Card] = Deck.shuffle()) {
  private var _discarded: List[Card] = List.empty
  private var _dealt: List[Card] = List.empty
  private var _burned: List[Card] = List.empty

  def cards = _cards
  def deal(n: Int): List[Card] = {
    val cards = _cards take n
    _cards = _cards diff cards
    cards
  }

  def reshuffle() {
    _cards = Random shuffle (_cards ++ _burned)
    _burned = List.empty
  }

  def share(n: Int): List[Card] = {
    val cards = deal(n)
    _dealt ++= cards
    cards
  }

  def burn(n: Int) {
    _burned ++= deal(n)
  }

  def burn(cards: List[Card]) {
    _cards = _cards diff cards
    _burned ++= cards
  }

  def without(cards: List[Card]) = new Deck(_cards diff cards)

  @throws[Deck.NoCardsLeft]
  def discard(old: List[Card]): List[Card] = {
    val n = old.size

    if (n > _cards.size + _burned.size) throw Deck.NoCardsLeft()

    if (n > _cards.size)
      reshuffle()

    val cards = deal(n)
    _burned ++= old
    _dealt = _dealt diff old
    cards
  }
}
