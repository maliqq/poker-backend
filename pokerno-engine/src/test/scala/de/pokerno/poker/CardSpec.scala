package de.pokerno.poker

import org.scalatest._
import org.scalatest.Matchers._

class CardSpec extends FunSpec {
  describe("Card") {
    it("all") {
      Card.CardsNum should equal(52)
      All.size should equal(Card.CardsNum)
    }

    describe("parse int") {
      it("invalid") {
        an [Card.Invalid] should be thrownBy(Card.fromInt(-1))
      }
      
      it("deuce") {
        val card = Card.fromInt(0)
        card.toInt should equal(0)
        card.kind should equal(Kind.Value.Deuce)
        card.suit should equal(Suit.Spade)
      }

      it("ace") {
        val card = Card.fromInt(51)
        card.toInt should equal(51)
        card.kind should equal(Kind.Value.Ace)
        card.suit should equal(Suit.Club)
      }
    }

    describe("parse string") {
      it("invalid suit") {
        an [Card.ParseError] should be thrownBy(Card.fromString("Kj"))
      }
      it("invalid kind") {
        an [Card.ParseError] should be thrownBy(Card.fromString("Xh"))
      }
      
      it("parse string") {
        val card = Card.fromString("8c")
        card.kind should equal(Kind.Value.Eight)
        card.suit should equal(Suit.Club)
      }
    }

    it("comparing") {
      Card.fromInt(0) should equal(Card.fromInt(0))
      Card.fromString("As") should equal(Card.fromString("As"))
    }
  }

  describe("Cards") {
    it("parse empty result") {
      Cards.fromString("hello") shouldBe empty
    }
    
    it("parse string") {
      Cards.fromString("AhKhQhJhTh").map(_.toInt) should equal(List(49, 45, 41, 37, 33))
    }

    it("parse List[Int]") {
      val l = List(1, 2, 3, 4, 5)
      Cards.fromSeq(l).map(_.toInt) should equal(l)
    }
    
    it("to binary") {
      val cards = Cards.fromString("2s2h2d2c")
      (cards: Array[Byte]) should equal(Array(1, 2, 3, 4))
    }
  }
}
