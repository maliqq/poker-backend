package de.pokerno.poker

import util.Random

object Deck {
  final val FullBoardSize = 5

  def shuffle(cards: Seq[Card] = Cards) = Random.shuffle(cards)
  def apply() = shuffle()

  case class NoCardsLeft() extends Exception("No cards left in deck")
}

class Deck(private var _cards: Seq[Card] = Deck.shuffle()) {
  private var _discarded: Seq[Card] = List.empty
  private var _dealt: Seq[Card] = List.empty
  private var _burned: Seq[Card] = List.empty

  def cards = _cards
  def deal(n: Int): Seq[Card] = {
    val cards = _cards take n
    _cards = _cards diff cards
    cards
  }

  def reshuffle() {
    _cards = Random shuffle (_cards ++ _burned)
    _burned = List.empty
  }

  def share(n: Int): Seq[Card] = {
    val cards = deal(n)
    _dealt ++= cards
    cards
  }

  def burn(n: Int) {
    _burned ++= deal(n)
  }

  def burn(cards: Seq[Card]) {
    _cards = _cards diff cards
    _burned ++= cards
  }

  def without(cards: Seq[Card]) = new Deck(_cards diff cards)

  @throws[Deck.NoCardsLeft]
  def discard(old: Seq[Card]): Seq[Card] = {
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
