package pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class CardSpec extends FlatSpec with ClassicMatchers {
  "Card" should "be" in {
    Console.printf("%s", Card.All)
    Card.All.size should equal(52)
  }
}
