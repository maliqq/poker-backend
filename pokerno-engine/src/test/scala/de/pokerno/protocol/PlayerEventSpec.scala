package de.pokerno.protocol

import org.scalatest._
import org.scalatest.Matchers._
import de.pokerno.protocol.{action => message}

class PlayerEventSpec extends FunSpec {
  
  import de.pokerno.poker.Cards
  
  describe("PlayerEvent") {
    it("AddBet") {
      val bet = PlayerEvent.decode("""{"bet":{"check":true}}""")
      bet match {
        case message.AddBet(bet) =>
          bet.isCheck should be(true)
        
        case _ =>
          throw new Exception("not bet")
      }
    }
    
    it("AddOn") {
      val buyIn = PlayerEvent.decode("""{"buyin":2000}""")
      buyIn match {
        case message.BuyIn(amount) =>
          amount should equal(2000)
        
        case _ =>
          throw new Exception("not buy in")
      }
    }
    
    it("BuyIn") {
      
    }
    
    it("Chat") {
      
    }
    
    it("ComeBack") {
      
    }
    
    it("DiscardCards") {
      val discard = PlayerEvent.decode("""{"discard":"MTMy"}""")
      discard match {
        case message.DiscardCards(cards) =>
          cards should equal(Cards.fromString("AsAdAh"))
        case _ =>
      }
    }
    
    it("DoubleRebuy") {
      
    }
    
    it("JoinTable") {
      
    }
    
    it("LeaveTable") {
      
    }
    
    it("Rebuy") {
      
    }
    
    it("ShowCards") {
      
    }
    
    it("SitOut") {
      
    }
  }
}
