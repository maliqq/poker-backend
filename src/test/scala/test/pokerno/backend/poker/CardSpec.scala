package test.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.poker.{ Card, Kind, Suit }

class CardSpec extends FunSpec with ClassicMatchers {
  describe("Card") {
    it("all") {
      Card.CardsNum should equal(52)
      Card.All.size should equal(Card.CardsNum)
    }
    
    it("parse int") {
      val card_2s = Card.parseInt(1)
      card_2s.kind should equal(Kind.Deuce)
      card_2s.suit should equal(Suit.Spade)
      
      val card_Ac = Card.parseInt(52)
      card_Ac.kind should equal(Kind.Ace)
      card_Ac.suit should equal(Suit.Club)
    }
    
    it("parse string") {
      val card_8c = Card.parseString("8c")
      card_8c.kind should equal(Kind.Eight)
      card_8c.suit should equal(Suit.Club)
    }
  }
}
