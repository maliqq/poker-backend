package de.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class MathSpec extends FunSpec with ClassicMatchers {
  describe("Math") {
    it("Sample") {
      val sample = new Math.Sample

      sample.mark(-1)
      sample.mark(-1)
      sample.mark(0)
      sample.mark(1)

      sample.loses should equal(.5)
      sample.ties should equal(.25)
      sample.wins should equal(.25)
    }
  }
}
