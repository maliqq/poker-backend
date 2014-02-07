package de.pokerno.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class SuitSpec extends FunSpec with ClassicMatchers {
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

      Suit.Seq should equal(List(0, 1, 2, 3))
      Suit.All should equal(List[Suit.Value](0, 1, 2, 3))

      Suit.All.map(_ toString) mkString ("") should equal("shdc")
      Suit.All.map(_ unicode) mkString ("") should equal("♠♥♦♣")
    }
  }
}
