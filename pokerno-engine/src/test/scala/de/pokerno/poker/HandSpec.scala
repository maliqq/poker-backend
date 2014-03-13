package de.pokerno.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class HandSpec extends FunSpec with ClassicMatchers {
  describe("Hand") {

  }

  describe("Hand.Cards") {
    it("has gaps") {
      val cards = CardUtils.parseString("AsKsQs7s8s9s").sorted(AceHigh)
      val handCards = new CardSet(cards)
      handCards.gaps.size should equal(3)
      val List(gap1, gap2, gap3) = handCards.gaps
      gap1 should equal(Cards("As"))
      gap2 should equal(Cards("7s8s9s"))
      gap3 should equal(Cards("QsKsAs"))
    }
  }
}
