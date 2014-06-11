package de.pokerno.poker

import org.scalatest._
import org.scalatest.Matchers._

class KindSpec extends FunSpec {
  describe("Kind") {
    it("all") {
      Kind.Value.values.min should equal(Kind.Value.Deuce)
      Kind.Value.values.max should equal(Kind.Value.Ace)
    }
  }
}
