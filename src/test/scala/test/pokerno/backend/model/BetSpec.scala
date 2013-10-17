package test.pokerno.backend.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.model.{ Bet, Stake }

class BetSpec extends FunSpec with ClassicMatchers {
  describe("Bet") {
    it("amount") {
      Bet.check.amount should equal(.0)
      Bet.fold.amount should equal(.0)
      Bet.call(1.0).amount should equal(1.0)
      Bet.raise(2.0).amount should equal(2.0)
    }

    it("bet type") {
      Bet.check.betType should equal(Bet.Check)
      Bet.fold.betType should equal(Bet.Fold)
      Bet.call(1.0).betType should equal(Bet.Call)
      Bet.raise(2.0).betType should equal(Bet.Raise)
    }

    it("bet to string") {
      Bet.check.toString should equal("Check")
      Bet.fold.toString should equal("Fold")
      Bet.call(1.0).toString should equal("Call 1.00")
      Bet.raise(2.0).toString should equal("Raise 2.00")
    }
    
    describe("isValid") {
      it("fold") {
      }
      
      it("check") { 
      }
      
      it("call") {
      }
      
      it("raise") {
      }
    }

//    it("force") {
//      val stake = new Stake(8.0, Ante = Right(true), BringIn = Right(true))
//      Bet.force(Bet.Ante, stake).amount should equal(1.0)
//      Bet.force(Bet.BringIn, stake).amount should equal(2.0)
//      Bet.force(Bet.SmallBlind, stake).amount should equal(4.0)
//      Bet.force(Bet.BigBlind, stake).amount should equal(8.0)
//    }
  }
}
