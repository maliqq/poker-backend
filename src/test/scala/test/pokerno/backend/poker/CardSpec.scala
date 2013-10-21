package test.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.poker.{ Card, Kind, Suit, Cards }

class CardSpec extends FunSpec with ClassicMatchers {
  describe("Card") {
    it("all") {
      Card.CardsNum should equal(52)
      Card.All.size should equal(Card.CardsNum)
    }

    it("parse int") {
      ;{
        val card = Card.parseInt(0)
        card.toInt should equal(0)
        card.kind should equal(Kind.Value.Deuce)
        card.suit should equal(Suit.Spade)
      }
      
      ;{
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
  }
  
  describe("Cards") {
    it("parse string") {
      Cards("AhKhQhJhTh").map(_.toInt) should equal(List(49, 45, 41, 37, 33))
    }
    
    it("parse List[Int]") {
      Cards(List(1,2,3,4,5)).map(_.toInt) should equal(List(1, 2, 3, 4, 5))
    }
  }
}
