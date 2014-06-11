package de.pokerno.poker

import org.scalatest._
import org.scalatest.Matchers._

class OrderingSpec extends FunSpec {
  describe("Ordering") {
    it("ace high") {
      val ace: Card = Card(51)
      val deuce: Card = Card(0)
      val cards: Cards = List(deuce, Card(2), Card(3), ace)
      cards.max(AceHigh) should equal(ace)
      cards.min(AceHigh) should equal(deuce)
    }

    it("ace low") {
      val ace: Card = Card(51)
      val deuce: Card = Card(0)
      val king: Card = Card(11)
      val cards: Cards = List(deuce, Card(2), king, ace)
      cards.max(AceLow) should equal(king)
      cards.min(AceLow) should equal(ace)
    }
  }

  describe("Ranking") {
    it("by rank") {
      val cards = new CardSet(List.empty)

      val h1 = cards.hand(rank = Some(Rank.High.Flush))
      val h2 = cards.hand(rank = Some(Rank.High.StraightFlush))

      val hands = List[Hand](h1, h2)

      hands max (Ranking) should equal(h2)
    }

    it("by high") {
      val cards = new CardSet(List.empty)

      val h1 = cards.hand(rank = Some(Rank.High.Flush), high = Left(List('_2d)))
      val h2 = cards.hand(rank = Some(Rank.High.Flush), high = Left(List('Kd)))

      val hands = List[Hand](h1, h2)

      hands.max should equal(h2)
    }

    it("by value") {
      val cards = new CardSet(List.empty)

      val h1 = cards.hand(rank = Some(Rank.High.Flush), high = Left(List(1)), value = List(1, 2, '_2d))
      val h2 = cards.hand(rank = Some(Rank.High.Flush), high = Left(List(1)), value = List(1, 2, 'Kd))

      val hands = List[Hand](h1, h2)

      hands.max should equal(h2)
    }

    it("by kicker") {
      val cards = new CardSet(List.empty)

      val h1 = cards.hand(rank = Some(Rank.High.Flush), high = Left(List(1)), value = List(1, 2, 3), kicker = Left(List('_2d)))
      val h2 = cards.hand(rank = Some(Rank.High.Flush), high = Left(List(1)), value = List(1, 2, 3), kicker = Left(List('Kd)))

      val hands = List[Hand](h1, h2)

      hands.max should equal(h2)
    }

    it("same hands") {
      val cards = new CardSet(List.empty)

      val h1 = cards.hand(rank = Some(Rank.High.Flush), high = Left(List(1)), value = List(1, 2, 3), kicker = Left(List(1)))
      val h2 = cards.hand(rank = Some(Rank.High.Flush), high = Left(List(1)), value = List(1, 2, 3), kicker = Left(List(1)))

      (h1 equals h2) should be(true)
    }
  }
}
