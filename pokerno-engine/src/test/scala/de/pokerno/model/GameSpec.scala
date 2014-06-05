package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class GameSpec extends FunSpec with ClassicMatchers {
  describe("Game") {
    it("max table size") {
      Game.MaxTableSize should equal(10)
    }

    it("full board size") {
      Game.FullBoardSize should equal(5)
    }

    it("new game with defaults") {
      Games.foreach {
        case (limited, options) ⇒
          val gameWithDefaults = new Game(limited)
          gameWithDefaults.limit should equal(options.defaultLimit)
          gameWithDefaults.tableSize should equal(options.maxTableSize)

          val gameWithExceededTableSize = Game(limited, tableSize = options.maxTableSize + 1)
          gameWithExceededTableSize.tableSize should equal(options.maxTableSize)
      }
    }

    it("toString") {
      Games.foreach {
        case (limited, options) ⇒
          val g1 = new Game(limited, Some(Game.FixedLimit), Some(6))
          g1.toString should equal(f"$limited ${Game.FixedLimit} 6-max")

          val g2 = new Game(limited, Some(Game.PotLimit), Some(6))
          g2.toString should equal(f"$limited ${Game.PotLimit} 6-max")

          val g3 = new Game(limited, Some(Game.NoLimit), Some(6))
          g3.toString should equal(f"$limited ${Game.NoLimit} 6-max")
      }
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

  describe("type checks") {
    it("Game.Limit") {
      Array(Game.NoLimit, Game.FixedLimit, Game.PotLimit) foreach { l ⇒
        l.isInstanceOf[Game.Limit] should be(true)
      }
    }
    it("Game.Limited") {
      Array(Game.Texas, Game.Omaha, Game.Omaha8,
        Game.Stud, Game.Stud8, Game.Razz, Game.London,
        Game.FiveCard, Game.Single27, Game.Triple27, Game.Badugi
      ) foreach { g ⇒
          g.isInstanceOf[Game.Limited] should be(true)
        }
    }

    it("Game.Mixed") {
      Array(Game.Horse, Game.Eight) foreach { g ⇒
        g.isInstanceOf[Game.Mixed] should be(true)
      }
    }

    it("groups") {
      Array(Game.Holdem, Game.SevenCard, Game.SingleDraw, Game.TripleDraw) foreach { g ⇒
        g.isInstanceOf[Game.Group] should be(true)
      }
    }

    it("group") {
      Array(Game.Texas, Game.Omaha, Game.Omaha8) foreach { g ⇒
        val o = Games(g)
        o.group should equal(Game.Holdem)
        o.hasBlinds should be(true)
        o.hasBoard should be(true)
        o.discards should be(false)
        o.hasAnte should be(false)
        o.hasBringIn should be(false)
        o.hasVela should be(false)
        o.maxTableSize should equal(10)
      }

      Array(Game.FiveCard, Game.Single27) foreach { g ⇒
        val o = Games(g)
        o.group should equal(Game.SingleDraw)
        o.discards should be(true)
        o.hasBlinds should be(true)
        o.hasBoard should be(false)
        o.hasAnte should be(false)
        o.hasBringIn should be(false)
        o.hasVela should be(false)
        o.maxTableSize should equal(6)
        o.defaultLimit should equal(Game.FixedLimit)
      }

      Array(Game.Triple27, Game.Badugi) foreach { g ⇒
        val o = Games(g)
        o.group should equal(Game.TripleDraw)
        o.discards should be(true)
        o.hasBlinds should be(true)
        o.hasBoard should be(false)
        o.hasAnte should be(false)
        o.hasBringIn should be(false)
        o.hasVela should be(false)
        o.maxTableSize should equal(6)
        o.defaultLimit should equal(Game.FixedLimit)
      }

      Array(Game.Stud, Game.Stud8, Game.Razz, Game.London) foreach { g ⇒
        val o = Games(g)
        o.group should equal(Game.SevenCard)
        o.discards should be(false)
        o.hasBlinds should be(false)
        o.hasBoard should be(false)
        o.hasAnte should be(true)
        o.hasBringIn should be(true)
        o.hasVela should be(true)
        o.pocketSize should equal(7)
        o.maxTableSize should equal(8)
        o.defaultLimit should equal(Game.FixedLimit)
      }
    }
  }

  describe("implicits") {
    it("limited") {
      ("texas": Option[Game.Limited]) should equal(Some(Game.Texas))
      ("omaha": Option[Game.Limited]) should equal(Some(Game.Omaha))
      ("omaha8": Option[Game.Limited]) should equal(Some(Game.Omaha8))
      ("stud": Option[Game.Limited]) should equal(Some(Game.Stud))
      ("stud8": Option[Game.Limited]) should equal(Some(Game.Stud8))
      ("razz": Option[Game.Limited]) should equal(Some(Game.Razz))
      ("london": Option[Game.Limited]) should equal(Some(Game.London))
      ("five-card": Option[Game.Limited]) should equal(Some(Game.FiveCard))
      ("single27": Option[Game.Limited]) should equal(Some(Game.Single27))
      ("triple27": Option[Game.Limited]) should equal(Some(Game.Triple27))
      ("badugi": Option[Game.Limited]) should equal(Some(Game.Badugi))
    }

    it("mixed") {
      ("horse": Option[Game.Mixed]) should equal(Some(Game.Horse))
      ("eight": Option[Game.Mixed]) should equal(Some(Game.Eight))
    }
  }

  describe("Limit") {
    // stack, bb, potSize
    def stack = 100000
    def bb = 100
    def potSize = 999

    describe("NoLimit") {
      Game.NoLimit.raise(stack, bb, potSize) should equal((bb, stack))
    }
    describe("FixedLimit") {
      Game.FixedLimit.raise(stack, bb, potSize) should equal((bb, bb))
    }
    describe("PotLimit") {
      Game.PotLimit.raise(stack, bb, potSize) should equal((bb, potSize))
    }
  }
}
