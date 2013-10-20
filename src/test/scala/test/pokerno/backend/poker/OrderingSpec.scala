package test.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.poker.{ AceHigh, AceLow, Card }

class OrderingSpec extends FunSpec with ClassicMatchers {
  describe("Ordering") {
    it("ace high") {
      val ace: Card = Card(51)
      val deuce: Card = Card(0)
      val cards: List[Card] = List(deuce, Card(2), Card(3), ace)
      cards.max(AceHigh) should equal(ace)
      cards.min(AceHigh) should equal(deuce)
    }
    
    it("ace low") {
      val ace: Card = Card(51)
      val deuce: Card = Card(0)
      val king: Card = Card(11)
      val cards: List[Card] = List(deuce, Card(2), king, ace)
      cards.max(AceLow) should equal(king)
      cards.min(AceLow) should equal(ace)
    }
  }
}
