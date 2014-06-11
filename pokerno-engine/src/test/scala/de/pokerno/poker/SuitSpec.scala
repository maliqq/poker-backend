package de.pokerno.poker

import org.scalatest._
import org.scalatest.Matchers._

class SuitSpec extends FunSpec {
  describe("Suit") {
    it("all") {
      val s: Suit.Value = 0
      s should equal(Suit.Spade)

      val h: Suit.Value = 1
      h should equal(Suit.Heart)

      val d: Suit.Value = 2
      d should equal(Suit.Diamond)

      val c: Suit.Value = 3
      c should equal(Suit.Club)

      Suit.NumericValues should equal(List(0, 1, 2, 3))
      Suits should equal(List[Suit.Value](0, 1, 2, 3))

      Suits.map(_ toString) mkString ("") should equal("shdc")
      Suits.map(_ unicode) mkString ("") should equal("♠♥♦♣")
    }
  }
}
