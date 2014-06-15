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
