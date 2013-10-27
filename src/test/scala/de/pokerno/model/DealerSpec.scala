package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class DealerSpec extends FunSpec with ClassicMatchers {
  describe("Dealer") {
    it("deal board") {
      val dealer = new Dealer
      dealer.board.size should equal(0)

      val cards = dealer.dealBoard(3)
      cards.size should equal(3)
    }

    it("deal pocket") {
      val dealer = new Dealer
      val player = new Player("1")
      val cards = dealer.dealPocket(2, player)
      cards.size should equal(2)
      dealer.pocket(player) should equal(cards)

      val discardedCards = dealer.discard(cards, player)
      dealer.pocket(player) should equal(discardedCards)
    }
  }
}
