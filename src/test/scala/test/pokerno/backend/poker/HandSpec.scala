package test.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.poker.{ Hand, Cards, AceHigh }

class HandSpec extends FunSpec with ClassicMatchers {
  describe("Hand") {
    
  }
  
  describe("Hand.Cards") {
    it("has gaps") {
      val cards = Cards.parseString("AsKsQs7s8s9s").sorted(AceHigh)
      val handCards = new Hand.Cards(cards)
      handCards.gaps.size should equal(3)
    }
  }
}
