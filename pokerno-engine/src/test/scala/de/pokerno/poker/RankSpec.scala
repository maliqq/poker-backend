package de.pokerno.poker

import org.scalatest._
import org.scalatest.Matchers._

class RankSpec extends FunSpec {
  describe("Rank.High") {
    it("ordered") {
      for (rank1 ← Rank.High.values; rank2 ← Rank.High.values) {
        (rank1 compare rank2) should equal(Rank.High.values.toList.indexOf(rank1).compare(Rank.High.values.toList.indexOf(rank2)))
      }
    }

    it("sorted") {
      Rank.High.values.max should equal(Rank.High.StraightFlush)
      Rank.High.values.min should equal(Rank.High.HighCard)
    }
  }

  describe("Rank.Badugi") {
    it("ordered") {
      for (rank1 ← Rank.Badugi.values; rank2 ← Rank.Badugi.values) {
        (rank1 compare rank2) should equal(Rank.Badugi.values.toList.indexOf(rank1).compare(Rank.Badugi.values.toList.indexOf(rank2)))
      }
    }

    it("sorted") {
      Rank.Badugi.values.max should equal(Rank.Badugi.BadugiFour)
      Rank.Badugi.values.min should equal(Rank.Badugi.BadugiOne)
    }
  }
}
