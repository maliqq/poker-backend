package de.pokerno.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class CardSpec extends FunSpec with ClassicMatchers {
  describe("Card") {
    it("all") {
      Card.CardsNum should equal(52)
      All.size should equal(Card.CardsNum)
    }

    describe("parse int") {
      it("deuce") {
        val card = Card.parseInt(0)
        card.toInt should equal(0)
        card.kind should equal(Kind.Value.Deuce)
        card.suit should equal(Suit.Spade)
      }

      it("ace") {
        val card = Card.parseInt(51)
        card.toInt should equal(51)
        card.kind should equal(Kind.Value.Ace)
        card.suit should equal(Suit.Club)
      }
    }

    it("parse string") {
      val card = Card.parseString("8c")
      card.kind should equal(Kind.Value.Eight)
      card.suit should equal(Suit.Club)
    }

    it("comparing") {
      Card.parseInt(0) should equal(Card.parseInt(0))
      Card.parseString("As") should equal(Card.parseString("As"))
    }
  }

  describe("Cards") {
    it("parse string") {
      Cards.fromString("AhKhQhJhTh").map(_.toInt) should equal(List(49, 45, 41, 37, 33))
    }

    it("parse List[Int]") {
      val l = List(1, 2, 3, 4, 5)
      Cards.fromSeq(l).map(_.toInt) should equal(l)
    }
  }
}
