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
      val horse = Mix(MixType.Horse)
      horse.games.size should equal(5)
      horse.games.foreach { g ⇒
        g.limit should equal(Limit.Fixed)
      }

      val eight = Mix(MixType.Eight)
      eight.games.size should equal(8)
    }
  }
}
