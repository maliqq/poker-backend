package de.pokerno.protocol

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._
import de.pokerno.protocol.{action => message}

class PlayerEventSpec extends FunSpec with ClassicMatchers {
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
