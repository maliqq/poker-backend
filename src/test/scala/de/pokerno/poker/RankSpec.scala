package de.pokerno.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class RankSpec extends FunSpec with ClassicMatchers {
  describe("Rank.High") {
    it("ordered") {
      for (rank1 ← Rank.High.values; rank2 ← Rank.High.values) {
        (rank1.ordinal compare rank2.ordinal) should equal(Rank.High.values.indexOf(rank1).compare(Rank.High.values.indexOf(rank2)))
      }
    }

    it("sorted") {
      Rank.High.values.max should equal(Rank.High.StraightFlush)
      Rank.High.values.min should equal(Rank.High.HighCard)
    }

    it("enum") {
    }
  }

  describe("Rank.Badugi") {
    it("ordered") {
      for (rank1 ← Rank.Badugi.values; rank2 ← Rank.Badugi.values) {
        (rank1.ordinal compare rank2.ordinal) should equal(Rank.Badugi.values.indexOf(rank1).compare(Rank.Badugi.values.indexOf(rank2)))
      }
    }

    it("sorted") {
      Rank.Badugi.values.max should equal(Rank.Badugi.BadugiFour)
      Rank.Badugi.values.min should equal(Rank.Badugi.BadugiOne)
    }
  }
}
