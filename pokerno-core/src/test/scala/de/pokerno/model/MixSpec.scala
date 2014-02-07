package de.pokerno.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class MixSpec extends FunSpec with ClassicMatchers {
  describe("Mix") {
    it("max table size") {
      Mix.MaxTableSize should equal(8)
    }

    it("new") {
      val horse = new Mix(Game.Horse)
      horse.games.size should equal(5)
      horse.games.foreach { g ⇒
        g.limit should equal(Game.FixedLimit)
      }

      val eight = new Mix(Game.Eight)
      eight.games.size should equal(8)
    }
  }
}
