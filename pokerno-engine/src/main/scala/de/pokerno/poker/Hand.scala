package de.pokerno.poker

object Hand {
  implicit def cards2CardSet(v: List[Card]): CardSet = new CardSet(v)

  case class InvalidCards(str: String) extends Exception(str)

  sealed trait Ranking {
    def apply(cards: List[Card]): Option[Hand]
  }

  abstract class HighRanking extends Ranking
  case object High extends HighRanking {
    def apply(cards: List[Card]): Option[Hand] = {
      (new CardSet(cards, AceHigh) with HighHand) isHigh
    }
  }

  abstract class LowRanking extends Ranking
  case object AceFive extends LowRanking {
    def apply(cards: List[Card]) = None
  }
  case object AceFive8 extends LowRanking {
    def apply(cards: List[Card]) = None
  }
  case object AceSix extends LowRanking {
    def apply(cards: List[Card]) = None
  }
  case object DeuceSix extends LowRanking {
    def apply(cards: List[Card]) = None
  }
  case object DeuceSeven extends LowRanking {
    def apply(cards: List[Card]) = None
  }

  abstract class BadugiRanking extends Ranking
  case object Badugi extends BadugiRanking {
    def apply(cards: List[Card]): Option[Hand] = {
      (new CardSet(cards) with BadugiHand).isBadugi
    }
  }
}

class Hand(
    val cards: CardSet,
    val value: List[Card] = List.empty,
    var rank: Option[Rank.Value] = None,
    High: Either[List[Card], Boolean] = Right(false),
    Kicker: Either[List[Card], Boolean] = Right(false)) extends Ordered[Hand] {

  val kicker: List[Card] = Kicker match {
    case Left(_cards) ⇒ _cards
    case Right(true)  ⇒ cards.value.diff(value).sorted(cards.ordering).reverse.take(5 - value.size)
    case Right(false) ⇒ List.empty
  }

  val high: List[Card] = High match {
    case Left(_cards) ⇒ _cards
    case Right(true)  ⇒ value.sorted(cards.ordering).reverse.take(1)
    case Right(false) ⇒ List.empty
  }

  def ranked(r: Rank.Value) = {
    rank = Some(r)
    Some(this)
  }

  def compare(other: Hand): Int = Ranking.compare(this, other)

  private def equalKinds(a: List[Card], b: List[Card]): Boolean = {
    if (a.size != b.size) return false

    a.zipWithIndex foreach {
      case (card, i) ⇒
        val otherCard = b(i)
        if (card.kind != otherCard.kind) return false
    }

    return true
  }

  override def equals(o: Any): Boolean = o match {
    case other: Hand ⇒
      rank.get == other.rank.get &&
        equalKinds(high, other.high) &&
        equalKinds(value, other.value) &&
        equalKinds(kicker, other.kicker)
    case _ ⇒ false
  }

  override def toString = "rank=%s high=%s value=%s kicker=%s" format (rank, high, value, kicker)

  def description: String = rank.get match {
    case Rank.High.HighCard ⇒
      "high card %s" format high.head.kind

    case Rank.High.OnePair ⇒
      "pair of %ss" format high.head.kind

    case Rank.High.TwoPair ⇒
      "two pairs, %ss and %ss" format (high.head.kind, high(1).kind)

    case Rank.High.ThreeKind ⇒
      "three of a kind, %ss" format high.head.kind

    case Rank.High.Straight ⇒
      "straight, %s to %s" format (value.min.kind, value.max.kind)

    case Rank.High.Flush ⇒
      "flush, %s high" format high.head.kind

    case Rank.High.FullHouse ⇒
      "full house, %ss full of %ss" format (high.head.kind, high(1).kind)

    case Rank.High.FourKind ⇒
      "four of a kind, %ss" format high.head.kind

    case Rank.High.StraightFlush ⇒
      "straight flush, %s to %s" format (value.min.kind, value.max.kind)

    case Rank.Badugi.BadugiOne ⇒
      "1-card badugi: %s" format (value head)

    case Rank.Badugi.BadugiTwo ⇒
      "2-card badugi: %s + %s" format (value.head, value(1))

    case Rank.Badugi.BadugiThree ⇒
      "3-card badugi: %s + %s + %s" format (value.head, value(1), value(2))

    case Rank.Badugi.BadugiFour ⇒
      "4-card badugi: %s + %s + %s + %s" format (value.head, value(1), value(2), value(3))

    case _ ⇒
      "(unknown: %s)" format this
  }
}