package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class GameSpec extends FunSpec with ClassicMatchers {
  describe("Game") {
    it("max table size") {
      Game.MaxTableSize should equal(10)
    }

    it("new game with defaults") {
      Games.Default.foreach {
        case (limited, options) ⇒
          val gameWithDefaults = new Game(limited)
          gameWithDefaults.limit should equal(options.defaultLimit)
          gameWithDefaults.tableSize should equal(options.maxTableSize)

          val gameWithExceededTableSize = new Game(limited, TableSize = Some(options.maxTableSize + 1))
          gameWithExceededTableSize.tableSize should equal(options.maxTableSize)
      }
    }

    it("implicits") {
      val g: Option[Game.Limited] = "texas"
      g should equal(Game.Texas)

      val m: Option[Game.Mixed] = "horse"
      m should equal(Game.Horse)
    }
  }

  describe("Game.Limit") {
    it("raise") {
      val bb = 20
      val stack = 1000
      val pot = 350; {
        val (min, max) = Game.NoLimit.raise(stack, bb, pot)
        min should equal(bb)
        max should equal(stack)
      }; {
        val (min, max) = Game.NoLimit.raise(stack, bb, pot)
        min should equal(bb)
        max should equal(stack)
      }; {
        val (min, max) = Game.PotLimit.raise(stack, bb, pot)
        min should equal(bb)
        max should equal(pot)
      }; {
        val (min, max) = Game.PotLimit.raise(stack, bb, pot)
        min should equal(bb)
        max should equal(pot)
      }; {
        val (min, max) = Game.FixedLimit.raise(stack, bb, pot)
        min should equal(bb)
        max should equal(bb)
      }
    }
  }
}
