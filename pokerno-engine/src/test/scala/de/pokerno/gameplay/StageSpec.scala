package de.pokerno.gameplay

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class StageSpec extends FunSpec with ClassicMatchers {
  import Stages._

  describe("Stage") {
    it("sample") {
      val stages = stage("x") { ctx ⇒
        Stage.Next
      } ~> stage("y") { ctx ⇒
        Stage.Next
      }

      stages.stages.size should equal(2)
    }
  }
}
