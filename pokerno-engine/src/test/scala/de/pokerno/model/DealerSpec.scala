package de.pokerno.model

import org.scalatest._
import org.scalatest.Matchers._

class DealerSpec extends FunSpec {
  describe("Dealer") {
    it("deal board") {
      val dealer = new Dealer
      dealer.board.size should equal(0)

      val cards1 = dealer.dealBoard(3)
      cards1.size should equal(3)
    }

    it("deal pocket") {
      val dealer = new Dealer
      val player = new Player("A")
      val cards = dealer.dealPocket(2, player)
      cards.size should equal(2)
      dealer.pocket(player) should equal(cards)
    }
    
    it("discard") {
      val dealer = new Dealer
      val player = new Player("A")
      val cards = dealer.dealPocket(2, player)
      
      val discarded = dealer.discard(List(cards.head), player)
      discarded.size should equal(2)
      val newCards = dealer.pocket(player)
      newCards should equal(discarded)
      newCards.head shouldNot equal(cards.head) // first card changed
      newCards.last should equal(cards.last) // last did't change
    }
  }
}
