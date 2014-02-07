package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class BetSpec extends FunSpec with ClassicMatchers {
  describe("Bet") {
    it("amount") {
      Bet.check.amount should equal(.0)
      Bet.fold.amount should equal(.0)
      Bet.allin.amount should equal(.0)
      Bet.call(1.0).amount should equal(1.0)
      Bet.raise(2.0).amount should equal(2.0)
      Bet.sb(1.0).amount should equal(1.0)
      Bet.bb(2.0).amount should equal(2.0)
      Bet.ante(1.0).amount should equal(1.0)
    }

    it("bet type") {
      Bet.check.betType should equal(Bet.Check)
      Bet.fold.betType should equal(Bet.Fold)
      Bet.call(1.0).betType should equal(Bet.Call)
      Bet.raise(2.0).betType should equal(Bet.Raise)
      Bet.sb(1.0).betType should equal(Bet.SmallBlind)
      Bet.bb(1.0).betType should equal(Bet.BigBlind)
      Bet.ante(1.0).betType should equal(Bet.Ante)
      Bet.allin.betType should equal(Bet.AllIn)
    }

    it("bet to string") {
      Bet.check.toString should equal("Check")
      Bet.fold.toString should equal("Fold")
      Bet.call(1.0).toString should equal("Call 1.00")
      Bet.raise(2.0).toString should equal("Raise 2.00")
    }

    describe("isValid") {
      it("fold") {
        Bet.fold.isValid(1000, 10, 10, (10.0, 10.0)) should be(true)
      }

      it("check") {
        Bet.check.isValid(1000, 10, 10, (.0, .0)) should be(true)
        Bet.check.isValid(1000, 10, 100, (.0, .0)) should be(false)
      }

      it("call") {
        val range: Range = (.0, .0)
        val stack = 1000
        val put = 10
        Bet.call(100).isValid(stack, put, 100, range) should be(true)
        Bet.call(2000).isValid(stack, put, 100, range) should be(false)
        Bet.call(100).isValid(stack, put, 200, range) should be(false)
      }

      it("raise") {
        val range: Range = (100.0, 200.0)
        val stack = 1000
        val put = 50
        Bet.raise(100).isValid(stack, put, 100, range) should be(true)
        Bet.raise(300).isValid(stack, put, 100, range) should be(false)
        Bet.raise(50).isValid(stack, put, 200, range) should be(false)
      }
    }

    it("type checks") {
      Array(
        Bet.SmallBlind, Bet.BigBlind, Bet.Ante, Bet.BringIn, Bet.GuestBlind, Bet.Straddle
      ) foreach { b ⇒
          b.isInstanceOf[Bet.ForcedBet] should be(true)
          val bet = Bet.forced(b, 1.0)
          bet.isForced should be(true)
        }

      Array(
        Bet.Check, Bet.Fold
      ) foreach { b ⇒
          b.isInstanceOf[Bet.PassiveBet] should be(true)
        }

      Array(
        Bet.Raise, Bet.Call, Bet.AllIn
      ) foreach { b ⇒
          b.isInstanceOf[Bet.ActiveBet] should be(true)
        }
    }
  }
}
