package test.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.poker.{ Kind }

class KindSpec extends FunSpec with ClassicMatchers {
  describe("Kind") {
    it("all") {
      Kind.Value.values.min should equal(Kind.Value.Deuce)
      Kind.Value.values.max should equal(Kind.Value.Ace)
    }
  }
}
