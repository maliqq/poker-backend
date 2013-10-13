package test.pokerno.backend.model

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.model.{Pot, SidePot}

class PotSpec extends FunSpec with ClassicMatchers {
  describe("Pot") {
    it("new") {
      val pot = new Pot
      pot.total should equal(0)
    }
    
    it("add") {
      val pot = new Pot
      
    }
  }
}
