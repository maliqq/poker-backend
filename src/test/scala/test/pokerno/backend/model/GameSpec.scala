package test.pokerno.backend.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.model.{Game, Games}

class GameSpec extends FunSpec with ClassicMatchers {
  describe("Game") {
    it("max table size") {
      Game.MaxTableSize should equal(10)
    }
    
    it("new game with defaults") {
      Games.Default.foreach { case (limited, options) =>
        val gameWithDefaults = new Game(limited)
        gameWithDefaults.limit should equal(options.defaultLimit)
        gameWithDefaults.tableSize should equal(options.maxTableSize)
        
        val gameWithExceededTableSize = new Game(limited, TableSize = Some(options.maxTableSize + 1))
        gameWithExceededTableSize.tableSize should equal(options.maxTableSize)
      }
    }
    
    it("implicits") {
      val g: Game.Limited = "texas"
      g should equal(Game.Texas)
      
      val m: Game.Mixed = "horse"
      m should equal(Game.Horse)
    }
  }
  
  describe("Game.Limit") {
    it("raise") {
      val bb = 20
      val stack = 1000
      val pot = 350
      val nl = Game.NoLimit.raise(stack, bb, pot, false)
      nl.min should equal(bb)
      nl.max should equal(stack)
      val nl_bigBets = Game.NoLimit.raise(stack, bb, pot, true)
      nl_bigBets.min should equal(bb)
      nl_bigBets.max should equal(stack)
      
      val pl = Game.PotLimit.raise(stack, bb, pot, false)
      pl.min should equal(bb)
      pl.max should equal(pot)
      
      val pl_bigBets = Game.PotLimit.raise(stack, bb, pot, true)
      pl_bigBets.min should equal(bb)
      pl_bigBets.max should equal(pot)
      
      val fl = Game.FixedLimit.raise(stack, bb, pot, false)
      fl.min should equal(bb)
      fl.max should equal(bb)
      
      val fl_bigBets = Game.FixedLimit.raise(stack, bb, pot, true)
      fl_bigBets.min should equal(bb * 2)
      fl_bigBets.max should equal(bb * 2)
    }
  }
}
