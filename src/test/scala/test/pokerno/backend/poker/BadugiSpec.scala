package test.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.poker.{ Rank, BadugiHand, Hand, Kind, Suit, Card }

class BadugiSpec extends FunSpec with ClassicMatchers {
  describe("Badugi") {
    // AaBbCcDd
    it("badugi four") {
      Kind.All.combinations(4) foreach {
        case comb ⇒
          Suit.All.permutations.foreach {
            case perm ⇒
              val value: List[Card] = comb.zip(perm) map { case (kind, suit) ⇒ Card.wrap(kind, suit) }
              val hand = Hand.Badugi(value)
              assert(hand.isDefined)
              val h = hand.get
              assert(h.rank.isDefined)
              h.rank.get should equal(Rank.Badugi.BadugiFour)
          }
      }
    }

    it("badugi three") {

    }

    it("badugi two") {

    }

    it("badugi one") {
      Kind.All.combinations(4) foreach { case comb =>
        Suit.All foreach { case suit =>
          val value: List[Card] = comb.map(Card.wrap(_, suit))
          val hand = Hand.Badugi(value)
          assert(hand.isDefined)
          val h = hand.get
          assert(h.rank.isDefined)
          h.rank.get should equal(Rank.Badugi.BadugiOne)
        }
      }
      
      Suit.All.permutations foreach { case perm =>
        Kind.All foreach { case kind =>
          val value: List[Card] = perm.map(Card.wrap(kind, _))
          val hand = Hand.Badugi(value)
          assert(hand.isDefined)
          val h = hand.get
          withClue("groupKind.size=%d".format(h.cards.groupKind.size)) { assert(h.rank.isDefined) }
          h.rank.get should equal(Rank.Badugi.BadugiOne)
        }
      }
    }
  }
}
