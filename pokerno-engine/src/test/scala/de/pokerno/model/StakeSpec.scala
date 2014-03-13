package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class StakeSpec extends FunSpec with ClassicMatchers {

  describe("Rates") {
    it("rates") {
      Rates(Bet.Ante).toDouble should equal(0.1)
      Rates(Bet.BringIn).toDouble should equal(0.25)
      Rates(Bet.SmallBlind).toDouble should equal(0.5)
      Rates(Bet.BigBlind).toDouble should equal(1.0)
    }
  }

  describe("Stake") {
    it("small blind") {
      val stake = new Stake(100)
      stake.smallBlind should equal(50)
    }

    it("ante true") {
      val stake = new Stake(100, Ante = Right(true))
      val anteRate = Rates(Bet.Ante)
      stake.ante.get should equal(100 * anteRate)
    }

    it("ante value") {
      val stake = new Stake(100, Ante = Left(10))
      stake.ante.get should equal(10)
    }

    it("bring in true") {
      val stake = new Stake(100, BringIn = Right(true))
      val bringInRate = Rates(Bet.BringIn)
      stake.bringIn.get should equal(100 * bringInRate)
    }

    it("bring in value") {
      val stake = new Stake(100, BringIn = Left(10))
      stake.bringIn.get should equal(10)
    }
  }

}