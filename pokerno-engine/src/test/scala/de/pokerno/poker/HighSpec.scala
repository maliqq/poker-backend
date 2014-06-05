package de.pokerno.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class HighSpec extends FunSpec with ClassicMatchers {
  describe("High") {

    def rainbow(kinds: List[Kind.Value.Kind]): Cards = {
      var cycle = Suits.iterator
      def nextSuit = if (cycle.hasNext) cycle.next else {
        cycle = Suits.iterator
        cycle.next
      }

      for {
        kind ← kinds
      } yield (Card(kind, nextSuit))
    }

    it("high card") {
      Kinds.foreach {
        case kind ⇒
          val tail = (Kinds.toSet - kind).toList.zipWithIndex.filter {
            case (v, i) ⇒ i % 2 == 0
          }.take(6).map(_._1)

          val cards = rainbow((kind :: tail))

          val hand = Hand.High(cards)
          assert(hand.isDefined)
          val h = hand.get
          assert(h.rank.isDefined)
          h.rank.get should equal(Rank.High.HighCard)
          h.value.size should equal(1)
          h.high.size should equal(1)
          h.high.head should equal(cards.max(AceHigh))
          h.kicker.size should equal(4)
      }
    }

    it("one pair") {
      Kinds.foreach {
        case kind ⇒
          val tail = (Kinds.toSet - kind).toList.zipWithIndex.filter {
            case (v, i) ⇒ i % 2 == 0
          }.take(5).map(_._1)

          val kinds = (kind :: kind :: tail)
          val cards = rainbow(kinds)

          val hand = Hand.High(cards)
          assert(hand.isDefined)
          val h = hand.get
          assert(h.rank.isDefined)
          h.rank.get should equal(Rank.High.OnePair)
          h.value.size should equal(2)
          h.high.size should equal(1)
          h.high.head.kind should equal(kind)
          h.kicker.size should equal(3)
      }
    }

    it("two pair") {
      Kinds.combinations(2) foreach {
        case comb ⇒
          val List(a, b, _*) = comb
          val kinds = List(a, a, b, b, (Kinds.toSet -- comb).head)
          val cards = rainbow(kinds)
          val hand = Hand.High(cards)
          assert(hand.isDefined)
          val h = hand.get
          assert(h.rank.isDefined)
          h.rank.get should equal(Rank.High.TwoPair)
          h.value.size should equal(4)
          h.high.size should equal(2)
          h.high.head should equal(h.high.max(AceHigh))
          h.high.last should equal(h.high.min(AceHigh))
          h.kicker.size should equal(1)
      }
    }

    it("three kind") {
      for (kind ← Kinds) {
        val kinds = List(kind, kind, kind) ++ (Kinds.toSet - kind).take(4).toList
        val cards = rainbow(kinds)
        val hand = Hand.High(cards)
        assert(hand.isDefined)
        val h = hand.get
        assert(h.rank.isDefined)
        h.rank.get should equal(Rank.High.ThreeKind)
        h.value.size should equal(3)
        h.high.size should equal(1)
        h.high.head.kind should equal(kind)
        h.kicker.size should equal(2)
      }
    }

    it("straight") {
      val deck: Cards = (
          for { kind ← Kinds }
          yield Card(kind, Suit.Spade)
        ).toList
        
      (1 to 6) foreach {
        case i ⇒
          val cards: Cards = deck.slice(i, i + 7).zipWithIndex map {
            case (card, i) ⇒
              if (i < 3) new Card(card.kind, Suit.Heart)
              else card
          }
          val hand: Option[Hand] = Hand.High(cards)
          assert(hand.isDefined)
          val h = hand.get
          assert(h.rank.isDefined)
          h.rank.get should equal(Rank.High.Straight)
          h.value.size should equal(5)
          h.high.head should equal(cards max (AceHigh))
          h.kicker.size should equal(0)
      }
    }

    val beFlush = new Matcher[Option[Hand]] {
      def apply(value: Option[Hand]): MatchResult = {
        assert(value.isDefined)

        val h = value.get
        assert(h.rank.isDefined)
        h.value.size should equal(5)
        h.high.head should equal(h.cards.value.max(AceHigh))
        h.kicker.size should equal(0)

        MatchResult(h.rank.get == Rank.High.Flush, "%s should be flush".format(value), "%s should not be flush".format(value))
      }
    }

    it("flush") {
      for (suit ← Suits) {
        val deck: Cards = (for { kind ← Kinds } yield Card(kind, suit)).toList
        (0 to 6) foreach {
          case i ⇒
            val cards = deck.slice(i, i + 7)
            val hc = new CardSet(cards) with HighHand
            val hand: Option[Hand] = hc.isFlush
            hand should beFlush
            if (hc.isStraight.isEmpty) {
              Hand.High(cards) should beFlush
            }
        }
      }
    }

    it("full house") {
      for (kind ← Kinds) {
        val set: Cards = Suits.take(3).map(Card(kind, _))
        val minor: Kind.Value.Kind = (Kinds.toSet - kind).head
        val pair: Cards = List(Card(minor, Suit.Diamond), Card(minor, Suit.Club))
        val value = set ++ pair
        val hand: Option[Hand] = Hand.High(value)
        assert(hand.isDefined)
        val h: Hand = hand.get
        assert(h.rank.isDefined)
        h.rank.get should equal(Rank.High.FullHouse)
        h.value should equal(value)
        h.high.map(_.kind) should equal(List(set.head.kind, pair.head.kind))
        h.kicker.size should equal(0)
      }
    }

    it("four kind") {
      for (kind ← Kinds) {
        val quad: Cards = for { suit ← Suits }
                               yield Card(kind, suit)
        val other3 = (Kinds.toSet - kind).take(3).map(Card(_, Suit.Spade))
        val value = quad ++ other3
        val hand: Option[Hand] = Hand.High(value)
        assert(hand.isDefined)
        val h = hand.get
        assert(h.rank.isDefined)
        h.rank.get should equal(Rank.High.FourKind)
        h.value should equal(quad)
        h.high.head.kind should equal(kind)
        h.kicker.size should equal(1)
        h.kicker.head should equal(other3 max (AceHigh))
      }
    }

    it("straight flush") {
      for (suit ← Suits) {
        val deck: Cards = (
            for { kind ← Kinds }
            yield Card(kind, suit)
          ).toList
          
        (1 to 6) foreach {
          case i ⇒
            val cards = deck.slice(i, i + 7)
            val hand: Option[Hand] = Hand.High(cards)
            assert(hand.isDefined)
            val h = hand.get
            assert(h.rank.isDefined)
            h.rank.get should equal(Rank.High.StraightFlush)
        }
      }
    }
  }
}
