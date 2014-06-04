package de.pokerno.poker

import beans._

import com.fasterxml.jackson.annotation.{JsonProperty, JsonInclude, JsonIgnore, JsonPropertyOrder}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.pokerno.protocol.Serializers.Cards2Binary

object Hand {
  implicit def cards2CardSet(v: Cards): CardSet = new CardSet(v)

  case class InvalidCards(str: String) extends Exception(str)

  sealed trait Ranking {
    def apply(cards: Cards): Option[Hand]
  }

  abstract class HighRanking extends Ranking
  case object High extends HighRanking {
    def apply(cards: Cards): Option[Hand] = {
      (new CardSet(cards, AceHigh) with HighHand) isHigh
    }
  }

  abstract class LowRanking extends Ranking
  case object AceFive extends LowRanking {
    def apply(cards: Cards) = None
  }
  case object AceFive8 extends LowRanking {
    def apply(cards: Cards) = None
  }
  case object AceSix extends LowRanking {
    def apply(cards: Cards) = None
  }
  case object DeuceSix extends LowRanking {
    def apply(cards: Cards) = None
  }
  case object DeuceSeven extends LowRanking {
    def apply(cards: Cards) = None
  }

  abstract class BadugiRanking extends Ranking
  case object Badugi extends BadugiRanking {
    def apply(cards: Cards): Option[Hand] = {
      (new CardSet(cards) with BadugiHand).isBadugi
    }
  }
}

@JsonPropertyOrder(Array("rank", "cards", "value", "high", "kicker", "description"))
@JsonInclude(JsonInclude.Include.NON_NULL)
class Hand(
    private val _cards: CardSet,
    @JsonSerialize(converter=classOf[Cards2Binary]) val value: Cards = Seq.empty,
    var rank: Option[Rank.Value] = None,
    _high: Either[Cards, Boolean] = Right(false),
    _kicker: Either[Cards, Boolean] = Right(false)) extends Ordered[Hand] {

  def cards = _cards
  
  @JsonSerialize(converter=classOf[Cards2Binary])
  @JsonProperty("cards")    def cardsValue: Array[Byte] = cards.value
  
  @JsonSerialize(converter=classOf[Cards2Binary]) val kicker: Cards = _kicker match {
    case Left(_cards) ⇒ _cards
    case Right(true)  ⇒ _cards.value.diff(value).sorted(cards.ordering).reverse.take(5 - value.size)
    case Right(false) ⇒ Seq.empty
  }
  
  @JsonSerialize(converter=classOf[Cards2Binary]) val high: Cards = _high match {
    case Left(_cards) ⇒ _cards
    case Right(true)  ⇒ value.sorted(cards.ordering).reverse.take(1)
    case Right(false) ⇒ Seq.empty
  }
  
  def ranked(r: Rank.Value) = {
    rank = Some(r)
    Some(this)
  }

  def compare(other: Hand): Int = Ranking.compare(this, other)

  private def equalKinds(a: Cards, b: Cards): Boolean = {
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

  @JsonProperty def description: String = rank.get match {
    case Rank.High.HighCard ⇒
      "high card %s" format high.head.kind

    case Rank.High.OnePair ⇒
      "pair of %ss" format high.head.kind

    case Rank.High.TwoPair ⇒
      "two pairs, %ss and %ss" format (high.head.kind, high(1).kind)

    case Rank.High.ThreeKind ⇒
      "three of a kind, %ss" format high.head.kind

    case Rank.High.Straight ⇒
      val (from, to) = if (high.head.kind == Kind.Value.Five)
        (value.min(AceLow).kind, value.max(AceLow).kind)
      else
        (value.min.kind, value.max.kind)

      "straight, %s to %s".format(from, to)

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
