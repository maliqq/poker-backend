package pokerno.backend.poker

object Hand {
  case class InvalidCards(str: String) extends Exception(str)

  sealed trait Ranking {
    def apply(cards: List[Card]): Option[Hand]
  }

  abstract class HighRanking extends Ranking
  case object High extends HighRanking {
    def apply(cards: List[Card]): Option[Hand] = {
      (new Cards(cards) with HighHand) isHigh
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
      (new Cards(cards) with BadugiHand).isBadugi
    }
  }

  class Cards(val value: List[Card]) {
    lazy val gaps = groupByGaps
    lazy val groupKind: Map[Kind.Value, List[Card]] = value.groupBy(_.kind)
    lazy val groupSuit: Map[Suit.Value, List[Card]] = value.groupBy(_.suit)
    lazy val paired = countGroups(groupKind)
    lazy val suited = countGroups(groupSuit)

    private def countGroups(groups: Map[_ <: Any, List[Card]]): Map[Int, List[List[Card]]] = {
      var _counter: Map[Int, List[List[Card]]] = Map.empty
      groups foreach {
        case (k, v) ⇒
          val count = v size
          val group = _counter getOrElse (count, List.empty)
          _counter += (count -> (group ++ List(v)))
      }
      _counter
    }
    
    private def groupByGaps: List[List[Card]] = { 
      var _gaps = List[List[Card]]()
      val (_, _buffer: List[Card]) = value.foldRight((value.head, List[Card]())) {
        case (card, (prev: Card, buffer: List[Card])) ⇒
          val d = card.toInt - prev.toInt
          if (d == 0)
            (prev, buffer)
          else if (d == 1 || d == -12)
            (prev, buffer ++ List(card))
          else {
            _gaps ::= buffer
            (card, List[Card]())
          }
      }
      _gaps ++ List(_buffer)
    }

    override def toString = "gaps=%s paired=%s suited=%s" format (gaps, paired, suited)
  }
}

class Hand(
    val cards: Hand.Cards = new Hand.Cards(List empty),
    val value: List[Card] = List.empty,
    var rank: Option[Rank.Type] = None,
    var high: List[Card] = List.empty,
    var kicker: List[Card] = List.empty,
    _rank: Boolean = false,
    _high: Boolean = false,
    _kicker: Boolean = false) extends Ordered[Hand] {

  if (_kicker && kicker.isEmpty)
    kicker = new CardSet(cards.value) kick (value)

  if (_high && high.isEmpty)
    high = value take (1)

  def ranked(r: Rank.Type) = {
    rank = Some(r)
    Some(this)
  }

  override def compare(other: Hand) = 1 compare 2
  override def toString = "rank=%s high=%s value=%s kicker=%s" format (rank, high, value, kicker)

  def description: String = rank.get match {
    case Rank.HighCard ⇒
      "high card %s" format (high.head.kind)

    case Rank.OnePair ⇒
      "pair of %ss" format (high.head.kind)

    case Rank.TwoPair ⇒
      "two pairs, %ss and %ss" format (high.head.kind, high(1).kind)

    case Rank.ThreeKind ⇒
      "three of a kind, %ss" format (high.head.kind)

    case Rank.Straight ⇒
      "straight, %s to %s" format (value.min.kind, value.max.kind)

    case Rank.Flush ⇒
      "flush, %s high" format (high.head.kind)

    case Rank.FullHouse ⇒
      "full house, %ss full of %ss" format (high.head.kind, high(1).kind)

    case Rank.FourKind ⇒
      "four of a kind, %ss" format (high.head.kind)

    case Rank.StraightFlush ⇒
      "straight flush, %s to %s" format (value.min.kind, value.max.kind)

    case Rank.BadugiOne ⇒
      "1-card badugi: %s" format (value head)

    case Rank.BadugiTwo ⇒
      "2-card badugi: %s + %s" format (value head, value(1))

    case Rank.BadugiThree ⇒
      "3-card badugi: %s + %s + %s" format (value head, value(1), value(2))

    case Rank.BadugiFour ⇒
      "4-card badugi: %s + %s + %s + %s" format (value head, value(1), value(2), value(3))
  }
}
