package de.pokerno.poker

import org.scalatest._
import org.scalatest.Matchers._

class MathSpec extends FunSpec {
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
