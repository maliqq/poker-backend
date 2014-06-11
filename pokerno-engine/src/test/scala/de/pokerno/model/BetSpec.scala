package de.pokerno.model

import org.scalatest._
import org.scalatest.Matchers._

class BetSpec extends FunSpec {
  describe("Bet") {
    it("amount") {
      Bet.call(1.0).amount should equal(1.0)
      Bet.raise(2.0).amount should equal(2.0)
      Bet.sb(1.0).amount should equal(1.0)
      Bet.bb(2.0).amount should equal(2.0)
      Bet.ante(1.0).amount should equal(1.0)
    }
    
    it("bet type") {
      Bet.check should equal(Bet.Check)
      Bet.fold should equal(Bet.Fold)
      Bet.call(1.0) should equal(Bet.Call(1.0))
      Bet.raise(2.0) should equal(Bet.Raise(2.0))
      Bet.sb(1.0) should equal(Bet.SmallBlind(1.0))
      Bet.bb(1.0) should equal(Bet.BigBlind(1.0))
      Bet.ante(1.0) should equal(Bet.Ante(1.0))
      Bet.allIn should equal(Bet.AllIn)
    }

    it("bet to string") {
      Bet.check.toString should equal("Check")
      Bet.fold.toString should equal("Fold")
      Bet.call(1.0).toString should equal("Call 1.00")
      Bet.raise(2.0).toString should equal("Raise 2.00")
    }

    it("type checks") {
      Array(
        Bet.SmallBlind, Bet.BigBlind, Bet.Ante, Bet.BringIn, Bet.GuestBlind, Bet.Straddle
      ) foreach { b ⇒
          b.isInstanceOf[Bet.Forced] should be(true)
          val bet = b.asInstanceOf[Bet.Forced]
          bet.isForced should be(true)
        }

      Array(
        Bet.Check, Bet.Fold
      ) foreach { b ⇒
          b.isInstanceOf[Bet.Passive] should be(true)
        }

      Array(
        Bet.Raise, Bet.Call, Bet.AllIn
      ) foreach { b ⇒
          b.isInstanceOf[Bet.Active] should be(true)
        }
    }
  }
}
