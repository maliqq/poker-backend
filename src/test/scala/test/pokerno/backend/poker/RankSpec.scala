package test.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.poker.{ Rank }

class RankSpec extends FunSpec with ClassicMatchers {
  describe("Rank.High") {
    it("ordered") {
      for (rank1 <- Rank.high; rank2 <- Rank.high) {
        rank1.compare(rank2) should equal (Rank.high.indexOf(rank1).compare(Rank.high.indexOf(rank2)))
      }
    }
    
    it("sorted") {
      Rank.high.max should equal(Rank.StraightFlush)
      Rank.high.min should equal(Rank.HighCard)
    }
  }
  
  describe("Rank.Badugi") {
    it("ordered") {
      for (rank1 <- Rank.badugi; rank2 <- Rank.badugi) {
        rank1.compare(rank2) should equal (Rank.badugi.indexOf(rank1).compare(Rank.badugi.indexOf(rank2)))
      }
    }
    
    it("sorted") {
      Rank.high.max should equal(Rank.BadugiFour)
      Rank.high.min should equal(Rank.BadugiOne)
    }
  }
}
