package de.pokerno.gameplay

import org.scalatest._
import org.scalatest.Matchers._

class NextTurnSpec extends FunSpec {
  
  import betting.NextTurn
  import de.pokerno.model._
  import de.pokerno.model.seat.impl._
  import de.pokerno.model.Seat.State
  
  describe("NextTurn") {
    it("SB ALLIN(5) BB(10)") {
      val sb = new Sitting(1, new Player("A"), State.Ready, Some(5))
      sb.postBet(Bet.SmallBlind(5))
      
      val bb = new Sitting(2, new Player("B"), State.Ready, Some(100))
      bb.postBet(Bet.BigBlind(10))
      
      val participants = List(sb, bb)
      
      NextTurn.decide(participants, 10)
    }
    
    it("SB(5) BB ALLIN(10)") {
      
    }
    
    it("SB(5) BB ALLIN(5)") {
      
    }
    
    it("SB(5) BB(10)") {
      
    }
  }
}
