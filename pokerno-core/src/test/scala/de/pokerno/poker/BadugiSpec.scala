package de.pokerno.poker

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.matchers.ShouldMatchers._

class BadugiSpec extends FunSpec with ClassicMatchers {
  describe("Badugi") {
    // AaBbCcDd
    val kinds = Kind.All
    val suits = Suit.All
    val kindCombinations = kinds.combinations(4)
    val suitPermutations = suits.permutations
    
    val beBadugi = new Matcher[Tuple2[List[Card], Rank.Value]] {
      def apply(arg: Tuple2[List[Card], Rank.Value]): MatchResult = {
        val (value: List[Card], rank: Rank.Value) = arg
        val hand = Hand.Badugi(value)
        assert(hand.isDefined)
        val h = hand.get
        assert(h.rank.isDefined)
        MatchResult(h.rank.get == rank,
            "%s should be %s; got: %s".format(h.cards, rank, h.rank.get),
            "%s should not be %s; got: %s".format(h.cards, rank, h.rank.get))
      }
    }
    
    val beBadugi1 = beBadugi compose { (c: List[Card]) =>
      (c, Rank.Badugi.BadugiOne)
    }
    
    val beBadugi2 = beBadugi compose { (c: List[Card]) =>
      (c, Rank.Badugi.BadugiTwo)
    }
    
    val beBadugi3 = beBadugi compose { (c: List[Card]) =>
      (c, Rank.Badugi.BadugiThree)
    }
    
    val beBadugi4 = beBadugi compose { (c: List[Card]) =>
      (c, Rank.Badugi.BadugiFour)
    }
    
    it("badugi four") {
      // ABCD
      kindCombinations foreach {
        case comb ⇒
          suitPermutations.foreach {
            case perm ⇒
              val value: List[Card] = comb.zip(perm) map { case (kind, suit) ⇒ Card.wrap(kind, suit) }
              value should beBadugi4
          }
      }
    }

    it("badugi three") {
      // ABCC
      kinds.combinations(3) foreach { case comb =>
        val value: List[Card] = (comb.head :: comb).zip(suits) map { case (kind, suit) => Card.wrap(kind, suit) }
        value should beBadugi3
      }
      
      // AxBxCD
      suits.combinations(3) foreach { case comb =>
        kindCombinations foreach { case kinds =>
          val value: List[Card] = kinds.zip(comb.head :: comb) map { case (kind, suit) => Card.wrap(kind, suit) }
          value should beBadugi3
        }
      }
    }

    it("badugi two") {
      // AAAB
      kinds.combinations(2) foreach { case comb =>
        val List(a, b) = comb
        val value: List[Card] = List(a, a, a, b).zip(suits) map {
          case (kind, suit) => Card.wrap(kind, suit)
        }
        value should beBadugi2
      }
      
      // AABB
      kinds.combinations(2) foreach { case comb =>
        val List(a, b) = comb
        val value: List[Card] = List(a, a, b, b).zip(suits) map {
          case (kind, suit) => Card.wrap(kind, suit)
        }
        value should beBadugi2
      }
      // AxBxCC
      kinds.combinations(3) foreach { case comb =>
        val List(a, b, c) = comb
        val List(s, h, d, _*) = suits
        val value: List[Card] = List(a, b, c, c).zip(List(s, s, h, d)) map {
          case (kind, suit) => Card.wrap(kind, suit)
        }
        value should beBadugi2
      }
    }

    it("badugi one") {
      // AxBxCxDx
      kindCombinations foreach { case comb =>
        suits foreach { case suit =>
          val value: List[Card] = comb.map(Card.wrap(_, suit))
          value should beBadugi1
        }
      }
      
      // AAAA
      suitPermutations foreach { case perm =>
        kinds foreach { case kind =>
          val value: List[Card] = perm.map(Card.wrap(kind, _))
          value should beBadugi1
        }
      }
    }
  }
}
