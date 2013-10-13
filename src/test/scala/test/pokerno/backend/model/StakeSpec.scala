package test.pokerno.backend.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.model.{Stake, Rates, Bet}

class StakeSpec extends FunSpec with ClassicMatchers {

  describe("Rates") {
    it("rates") {
      Rates.Default(Bet.Ante) should equal(0.125)
      Rates.Default(Bet.BringIn) should equal(0.25)
      Rates.Default(Bet.SmallBlind) should equal(0.5)
      Rates.Default(Bet.BigBlind) should equal(1.0)
      Rates.Default(Bet.DoubleBet) should equal(2.0)
    }
  }
  
  describe("Stake") {
    it("small blind") {
      val stake = new Stake(100)
      stake.smallBlind should equal(50)
    }
    
    it("double bet") {
      val stake = new Stake(100)
      val doubleBetRate = Rates.Default(Bet.DoubleBet)
      stake.doubleBet should equal(100 * doubleBetRate)
    }
    
    it("ante true") {
      val stake = new Stake(100, Ante = Right(true))
      val anteRate = Rates.Default(Bet.Ante)
      stake.ante.get should equal(100 * anteRate)
    }
    
    it("ante value") {
      val stake = new Stake(100, Ante = Left(10))
      stake.ante.get should equal(10)
    }
    
    it("bring in true") {
      val stake = new Stake(100, BringIn = Right(true))
      val bringInRate = Rates.Default(Bet.BringIn)
      stake.bringIn.get should equal(100 * bringInRate)
    }
    
    it("bring in value") {
      val stake = new Stake(100, BringIn = Left(10))
      stake.bringIn.get should equal(10)
    }
  }
  
}