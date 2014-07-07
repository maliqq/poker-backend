package de.pokerno.gameplay

import org.scalatest._
import org.scalatest.Matchers._

import de.pokerno.poker.{Cards, Hand}
import de.pokerno.model.{Player, SidePot}

class ShowdownSpec extends FunSpec {
  object Strategy extends stages.ShowdownStrategy {
  }
  
  describe("showdown strategy") {
    it("split equal hands") {
      val board = Cards.fromString("TdAsAdTsAc")
      val h1 = Strategy.rank(Cards.fromString("9c6h"), board, Hand.High)
      val h2 = Strategy.rank(Cards.fromString("5c5h"), board, Hand.High)
      val a = new Player("A")
      val b = new Player("B")
      val pot = new SidePot
      pot.add(a, 1000)
      pot.add(b, 1000)
      val best = Strategy.best(pot, Map(
          a -> h1,
          b -> h2
          ))
      Console printf("best=%s", best)
    }
  }
}
