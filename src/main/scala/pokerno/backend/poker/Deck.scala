package pokerno.backend.poker

import scala.util.Random

class Deck(private var _cards: List[Card] = List.empty) {
  private var _discarded: List[Card] = List.empty
  private var _dealt: List[Card] = List.empty
  private var _burned: List[Card] = List.empty
  
  def cards = _cards
  def deal(n: Int): List[Card] = {
    val cards = _cards.take(n)
    _cards = _cards diff cards
    cards
  }
  
  def reshuffle {
    _cards = Random.shuffle(_cards ++ _burned)
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
  
  def discard(old: List[Card]): List[Card] = {
    val n = old.length
    
    if (n > _cards.length + _burned.length)
      throw new Error("no cards left in deck")
    
    if (n > _cards.length)
      reshuffle
    
    val cards = deal(n)
    _burned ++= old
    _dealt = _dealt diff old
    cards
  }
}
