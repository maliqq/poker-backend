package test.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.poker.Card;

class CardSpec extends FlatSpec with ClassicMatchers {
  "Card" should "be" in {
    Card.All.size should equal(52)
  }
}
