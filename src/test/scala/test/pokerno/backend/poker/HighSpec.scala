package test.pokerno.backend.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

import pokerno.backend.poker._

class HighSpec extends FunSpec with ClassicMatchers {
  describe("High") {

    it("straight") {
      val deck: List[Card] = (for { kind ← Kind.All } yield Card.wrap(kind, Suit.Spade)).toList
      (1 to 6) foreach {
        case i ⇒
          val cards: List[Card] = deck.slice(i, i + 7).zipWithIndex map {
            case (card, i) ⇒
              if (i < 3) new Card(card.kind, Suit.Heart)
              else card
          }
          val hand: Option[Hand] = Hand.High(cards)
          hand.isDefined should be(true)
          val h = hand.get
          h.rank.get should equal(Rank.High.Straight)
          h.value.size should equal(5)
          h.high.head should equal(cards max (AceHigh))
          h.kicker.size should equal(0)
      }
    }

    it("flush") {
      for (suit ← Suit.All) {
        val deck: List[Card] = (for { kind ← Kind.All } yield Card.wrap(kind, suit)).toList
        (0 to 6) foreach {
          case i ⇒
            val deckSet = deck.toSet
            val cardsSet = deck.slice(i, i + 7).toSet
            val cards: List[Card] = (cardsSet - cardsSet.toList(3) + (deckSet -- cardsSet).head).toList
            val hand: Option[Hand] = Hand.High(cards)
            hand.isDefined should be(true)

            if (hand.get.rank == Rank.High.StraightFlush)
              cards should equal(List())

            val h = hand.get
            h.rank.get should equal(Rank.High.Flush)
            h.value.size should equal(5)
            h.high.head should equal(cards.max(AceHigh))
            h.kicker.size should equal(0)
        }
      }
    }

    it("full house") {
      for (kind ← Kind.All) {
        val set: List[Card] = Suit.All.take(3).map(Card.wrap(kind, _))
        val minor: Kind.Value.Kind = (Kind.All.toSet - kind).head
        val pair: List[Card] = List(Card.wrap(minor, Suit.Diamond), Card.wrap(minor, Suit.Club))
        val value = set ++ pair
        val hand: Option[Hand] = Hand.High(value)
        hand.isDefined should be(true)
        val h: Hand = hand.get

        h.rank.get should equal(Rank.High.FullHouse)
        h.value should equal(value)
        h.high.map(_.kind) should equal(List(set.head.kind, pair.head.kind))
        h.kicker.size should equal(0)
      }
    }

    it("four kind") {
      for (kind ← Kind.All) {
        val quad: List[Card] = for { suit ← Suit.All } yield Card.wrap(kind, suit)
        val other3 = (Kind.All.toSet - kind).take(3).map(Card.wrap(_, Suit.Spade))
        val value = quad ++ other3
        val hand: Option[Hand] = Hand.High(value)
        hand.isDefined should be(true)
        val h = hand.get

        h.rank.get should equal(Rank.High.FourKind)
        h.value should equal(quad)
        h.high.head.kind should equal(kind)

        //        val kickers = new CardSet(value) kick(h.value)
        //        kickers.size should equal(1)

        if (h.kicker.size == 0)
          value should equal(List())

        h.kicker.size should equal(1)
        h.kicker.head should equal(other3 max (AceHigh))
      }
    }

    it("straight flush") {
      for (suit ← Suit.All) {
        val deck: List[Card] = (for { kind ← Kind.All } yield Card.wrap(kind, suit)).toList
        (1 to 6) foreach {
          case i ⇒
            val cards = deck.slice(i, i + 7)
            val hand: Option[Hand] = Hand.High(cards)
            hand.isDefined should be(true)
            val h = hand.get
            h.rank.get should equal(Rank.High.StraightFlush)
        }
      }
    }
  }
}
