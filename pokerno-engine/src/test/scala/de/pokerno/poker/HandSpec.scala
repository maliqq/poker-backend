package de.pokerno.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class HandSpec extends FunSpec with ClassicMatchers {
  describe("Hand.Cards") {
    it("has gaps") {
      val cards = Cards.fromString("AsKsQs7s8s9s").sorted(AceHigh)
      val handCards = new CardSet(cards)
      handCards.gaps.size should equal(3)
      val Seq(gap1, gap2, gap3) = handCards.gaps
      gap1 should equal(Cards.fromString("As"))
      gap2 should equal(Cards.fromString("7s8s9s"))
      gap3 should equal(Cards.fromString("QsKsAs"))
    }
  }
}
