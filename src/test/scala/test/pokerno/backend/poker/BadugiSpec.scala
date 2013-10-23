package test.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.poker.{ Rank, BadugiHand, Hand, Kind, Suit, Card }

class BadugiSpec extends FunSpec with ClassicMatchers {
  describe("Badugi") {
    it("badugi four") {
      Kind.All.combinations(4) foreach {
        case comb ⇒
          Suit.All.permutations.foreach {
            case perm ⇒
              val value: List[Card] = comb.zip(perm) map { case (kind, suit) ⇒ Card.wrap(kind, suit) }
              val hand = Hand.Badugi(value)
              hand.isDefined should be(true)
              val h = hand.get
              h.rank.get should equal(Rank.Badugi.BadugiFour)
          }
      }
    }

    it("badugi three") {

    }

    it("badugi two") {

    }

    it("badugi one") {

    }
  }
}
