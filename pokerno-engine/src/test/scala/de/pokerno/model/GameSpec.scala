package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class GameSpec extends FunSpec with ClassicMatchers {
  import GameType._
  import MixType._

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
          val gameWithDefaults = Game(limited)
          gameWithDefaults.limit should equal(options.defaultLimit)
          gameWithDefaults.tableSize should equal(options.maxTableSize)

          val gameWithExceededTableSize = Game(limited, tableSize = options.maxTableSize + 1)
          gameWithExceededTableSize.tableSize should equal(options.maxTableSize)
      }
    }

    it("toString") {
      Games.foreach {
        case (limited, options) ⇒
          val g1 = Game(limited, Some(Limit.Fixed), Some(6))
          g1.toString should equal(f"$limited ${Limit.Fixed} 6-max")

          val g2 = Game(limited, Some(Limit.Pot), Some(6))
          g2.toString should equal(f"$limited ${Limit.Pot} 6-max")

          val g3 = Game(limited, Some(Limit.None), Some(6))
          g3.toString should equal(f"$limited ${Limit.None} 6-max")
      }
    }
  }

  describe("Game.Limit") {
    it("raise") {
      val bb = 20
      val stack = 1000
      val pot = 350; {
        val (min, max) = Limit.None.raise(stack, bb, pot)
        min should equal(bb)
        max should equal(stack)
      }; {
        val (min, max) = Limit.None.raise(stack, bb, pot)
        min should equal(bb)
        max should equal(stack)
      }; {
        val (min, max) = Limit.Pot.raise(stack, bb, pot)
        min should equal(bb)
        max should equal(pot)
      }; {
        val (min, max) = Limit.Pot.raise(stack, bb, pot)
        min should equal(bb)
        max should equal(pot)
      }; {
        val (min, max) = Limit.Fixed.raise(stack, bb, pot)
        min should equal(bb)
        max should equal(bb)
      }
    }
  }

  describe("type checks") {
    it("Game.Limit") {
      Array(Limit.None, Limit.Fixed, Limit.Pot) foreach { l ⇒
        l.isInstanceOf[Limit] should be(true)
      }
    }
    it("GameType") {
      Array(Texas, Omaha, Omaha8,
        Stud, Stud8, Razz, London,
        FiveCard, Single27, Triple27, Badugi
      ) foreach { g ⇒
          g.isInstanceOf[GameType] should be(true)
        }
    }

    it("Game.Mixed") {
      Array(MixType.Horse, MixType.Eight) foreach { g ⇒
        g.isInstanceOf[MixType] should be(true)
      }
    }

    it("groups") {
      Array(Game.Holdem, Game.SevenCard, Game.SingleDraw, Game.TripleDraw) foreach { g ⇒
        g.isInstanceOf[Game.Group] should be(true)
      }
    }

    it("group") {
      Array(Texas, Omaha, Omaha8) foreach { g ⇒
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

      Array(FiveCard, Single27) foreach { g ⇒
        val o = Games(g)
        o.group should equal(Game.SingleDraw)
        o.discards should be(true)
        o.hasBlinds should be(true)
        o.hasBoard should be(false)
        o.hasAnte should be(false)
        o.hasBringIn should be(false)
        o.hasVela should be(false)
        o.maxTableSize should equal(6)
        o.defaultLimit should equal(Limit.Fixed)
      }

      Array(Triple27, Badugi) foreach { g ⇒
        val o = Games(g)
        o.group should equal(Game.TripleDraw)
        o.discards should be(true)
        o.hasBlinds should be(true)
        o.hasBoard should be(false)
        o.hasAnte should be(false)
        o.hasBringIn should be(false)
        o.hasVela should be(false)
        o.maxTableSize should equal(6)
        o.defaultLimit should equal(Limit.Fixed)
      }

      Array(Stud, Stud8, Razz, London) foreach { g ⇒
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
        o.defaultLimit should equal(Limit.Fixed)
      }
    }
  }

  describe("implicits") {
    it("limited") {
      ("texas": GameType) should equal(Texas)
      ("omaha": GameType) should equal(Omaha)
      ("omaha8": GameType) should equal(Omaha8)
      ("stud": GameType) should equal(Stud)
      ("stud8": GameType) should equal(Stud8)
      ("razz": GameType) should equal(Razz)
      ("london": GameType) should equal(London)
      ("five-card": GameType) should equal(FiveCard)
      ("single27": GameType) should equal(Single27)
      ("triple27": GameType) should equal(Triple27)
      ("badugi": GameType) should equal(Badugi)
    }

    it("mixed") {
      ("horse": MixType) should equal(Horse)
      ("eight": MixType) should equal(Eight)
    }
  }

  describe("Limit") {
    // stack, bb, potSize
    def stack = 100000
    def bb = 100
    def potSize = 999

    describe("NoLimit") {
      Limit.None.raise(stack, bb, potSize) should equal((bb, stack))
    }
    describe("FixedLimit") {
      Limit.Fixed.raise(stack, bb, potSize) should equal((bb, bb))
    }
    describe("PotLimit") {
      Limit.Pot.raise(stack, bb, potSize) should equal((bb, potSize))
    }
  }
}
