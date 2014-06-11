package de.pokerno.model

import org.scalatest._
import org.scalatest.Matchers._

class SeatSpec extends FunSpec {

  describe("Seat") {
    it("toString") {
      val seat = new Seat
      seat.toString() should equal("")
    }
    
    it("put") {
      val seat = new Seat

      seat.player = new Player("1")
      seat.state should equal(Seat.State.Taken)

      seat.buyIn(1000)
      seat.state should equal(Seat.State.Ready)

      seat.play
      seat.state should equal(Seat.State.Play)

      seat.fold
      seat.state should equal(Seat.State.Fold)

      seat.play
      seat.check
      seat.state should equal(Seat.State.Bet)

      seat.play
      seat.raise(100)
      seat.put should equal(100)
      seat.stack should equal(900)
      seat.didCall(100.1) should be(false)
      seat.didCall(100) should be(true)
      seat.didCall(99.9) should be(true)

      seat.play
      seat.force(Bet.SmallBlind(25))
      seat.put should equal(25)
      seat.stack should equal(875)
    }

    it("all in raise") {
      val seat = new Seat

      seat.player = new Player("1")

      seat.buyIn(10)
      seat.raise(10)
      seat.state should equal(Seat.State.AllIn)
      seat.didCall(25) should be(true)
    }

    it("all in force") {
      val seat = new Seat

      seat.player = new Player("1")

      seat.buyIn(10)

      seat.play
      seat.force(Bet.SmallBlind(10))
      seat.state should equal(Seat.State.AllIn)
      seat.didCall(25) should be(true)

      seat.play
    }

    describe("state") {
      it("AllIn") {
        val seat = new Seat(Seat.State.AllIn)
        seat.isEmpty should be(false)
        seat.isActive should be(false)
        seat.isAllIn should be(true)
        seat.isEmpty should be(false)
        seat.isPlaying should be(false)
        seat.isReady should be(false)
        seat.isWaitingBB should be(false)
        seat.inPlay should be(false)
        seat.inPot should be(true)
      }

      it("Auto") {
        val seat = new Seat(Seat.State.Auto)
        seat.isEmpty should be(false)
        seat.isActive should be(false)
        seat.isAllIn should be(false)
        seat.isEmpty should be(false)
        seat.isPlaying should be(false)
        seat.isReady should be(false)
        seat.isWaitingBB should be(false)
        seat.inPlay should be(false)
        seat.inPot should be(false)
      }

      it("Away") {
        val seat = new Seat(Seat.State.Away)
        seat.isEmpty should be(false)
        seat.isActive should be(false)
        seat.isAllIn should be(false)
        seat.isEmpty should be(false)
        seat.isPlaying should be(false)
        seat.isReady should be(false)
        seat.isWaitingBB should be(false)
        seat.inPlay should be(false)
        seat.inPot should be(false)
      }

      it("Bet") {
        val seat = new Seat(Seat.State.Bet)
        seat.isEmpty should be(false)
        seat.isActive should be(false)
        seat.isAllIn should be(false)
        seat.isEmpty should be(false)
        seat.isPlaying should be(false)
        seat.isReady should be(false)
        seat.isWaitingBB should be(false)
        seat.inPlay should be(true)
        seat.inPot should be(true)
      }

      it("Empty") {
        val seat = new Seat(Seat.State.Empty)
        seat.isEmpty should be(true)
        seat.isActive should be(false)
        seat.isAllIn should be(false)
        seat.isEmpty should be(true)
        seat.isPlaying should be(false)
        seat.isReady should be(false)
        seat.isWaitingBB should be(false)
        seat.inPlay should be(false)
        seat.inPot should be(false)
      }

      it("Fold") {
        val seat = new Seat(Seat.State.Fold)
        seat.isEmpty should be(false)
        seat.isActive should be(false)
        seat.isAllIn should be(false)
        seat.isEmpty should be(false)
        seat.isPlaying should be(false)
        seat.isReady should be(true)
        seat.isWaitingBB should be(false)
        seat.inPlay should be(false)
        seat.inPot should be(false)
      }

      it("Idle") {
        val seat = new Seat(Seat.State.Idle)
        seat.isEmpty should be(false)
        seat.isActive should be(false)
        seat.isAllIn should be(false)
        seat.isEmpty should be(false)
        seat.isPlaying should be(false)
        seat.isReady should be(false)
        seat.isWaitingBB should be(false)
        seat.inPlay should be(false)
        seat.inPot should be(false)
      }

      it("Play") {
        val seat = new Seat(Seat.State.Play)
        seat.isEmpty should be(false)
        seat.isActive should be(true)
        seat.isAllIn should be(false)
        seat.isEmpty should be(false)
        seat.isPlaying should be(true)
        seat.isReady should be(true)
        seat.isWaitingBB should be(false)
        seat.inPlay should be(true)
        seat.inPot should be(true)
      }

      it("PostBB") {
        val seat = new Seat(Seat.State.PostBB)
        seat.isEmpty should be(false)
        seat.isActive should be(true)
        seat.isAllIn should be(false)
        seat.isEmpty should be(false)
        seat.isPlaying should be(false)
        seat.isReady should be(false)
        seat.isWaitingBB should be(false)
        seat.inPlay should be(false)
        seat.inPot should be(false)
      }

      it("Ready") {
        val seat = new Seat(Seat.State.Ready)
        seat.isEmpty should be(false)
        seat.isActive should be(false)
        seat.isAllIn should be(false)
        seat.isEmpty should be(false)
        seat.isPlaying should be(false)
        seat.isReady should be(true)
        seat.isWaitingBB should be(false)
        seat.inPlay should be(false)
        seat.inPot should be(false)
      }
    }

    describe("can") {
      it("bet") {

      }

      it("check") {

      }

      it("fold") {

      }

      it("call") {
        val seat = new Seat
        seat.player = new Player("1")
        seat.buyIn(1000)
        seat.play
        seat._canCall(500, 500) should be(true)
        seat._canCall(499, 500) should be(false)
        seat._canCall(500, 499) should be(false)
        seat._canCall(1000, 1000) should be(true)
        seat._canCall(1000, 1001) should be(true)
        seat._canCall(1001, 1002) should be(false)
        seat.raise(500)
        seat._canCall(500, 1000) should be(true)
        seat._canCall(500, 500) should be(false)
        seat.fold
        seat._canCall(500, 1000) should be(false)
      }

      it("raise") {

      }
    }
    
    describe("validate") {
//      it("fold") {
//        Bet.fold.isValid(1000, 10, 10, (10.0, 10.0)) should be(true)
//      }
//
//      it("check") {
//        Bet.check.isValid(1000, 10, 10, (.0, .0)) should be(true)
//        Bet.check.isValid(1000, 10, 100, (.0, .0)) should be(false)
//      }
//
//      it("call") {
//        val range: Range = (.0, .0)
//        val stack = 1000
//        val put = 10
//        Bet.call(100).isValid(stack, put, 100, range) should be(true)
//        Bet.call(2000).isValid(stack, put, 100, range) should be(false)
//        Bet.call(100).isValid(stack, put, 200, range) should be(false)
//      }
//
//      it("raise") {
//        val range: Range = (100.0, 200.0)
//        val stack = 1000
//        val put = 50
//        Bet.raise(100).isValid(stack, put, 100, range) should be(true)
//        Bet.raise(300).isValid(stack, put, 100, range) should be(false)
//        Bet.raise(50).isValid(stack, put, 200, range) should be(false)
//      }
    }
  }

}
