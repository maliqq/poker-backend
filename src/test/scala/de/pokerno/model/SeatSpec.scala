package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class SeatSpec extends FunSpec with ClassicMatchers {

  describe("Seat") {
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
      seat.amount should equal(900)
      seat.isCalled(100.1) should be(false)
      seat.isCalled(100) should be(true)
      seat.isCalled(99.9) should be(true)

      seat.play
      seat.force(25)
      seat.put should equal(25)
      seat.amount should equal(875)
    }

    it("all in raise") {
      val seat = new Seat

      seat.player = new Player("1")

      seat.buyIn(10)
      seat.raise(10)
      seat.state should equal(Seat.State.AllIn)
      seat.isCalled(25) should be(true)
    }

    it("all in force") {
      val seat = new Seat

      seat.player = new Player("1")

      seat.buyIn(10)

      seat.play
      seat.force(10)
      seat.state should equal(Seat.State.AllIn)
      seat.isCalled(25) should be(true)

      seat.play
    }
  }

}
