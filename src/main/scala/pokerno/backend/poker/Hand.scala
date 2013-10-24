package pokerno.backend.poker

object Hand {
  case class InvalidCards(str: String) extends Exception(str)

  sealed trait Ranking {
    def apply(cards: List[Card]): Option[Hand]
  }

  abstract class HighRanking extends Ranking
  case object High extends HighRanking {
    def apply(cards: List[Card]): Option[Hand] = {
      (new Cards(cards, AceHigh) with HighHand) isHigh
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

  class Cards(val value: List[Card], val ordering: Ordering[Card] = AceHigh) {
    lazy val gaps = groupByGaps
    lazy val groupKind: Map[Kind.Value.Kind, List[Card]] = value.groupBy(_.kind)
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
      val cards = value.filter(_.kind == Kind.Value.Ace) ++ value.sorted(AceHigh)
      val (_, _buffer: List[Card]) = cards.foldLeft((cards.head, List[Card]())) {
        case ((prev: Card, buffer: List[Card]), card) ⇒
          lazy val d = card.kind.toInt - prev.kind.toInt
          if (card == prev || d == 1 || d == -12)
            (card, buffer ++ List(card))
          else if (d == 0)
            (card, buffer)
          else {
            _gaps ++= List(buffer)
            (card, List[Card](card))
          }
      }
      _gaps ++ List(_buffer)
    }
    
    def hand() {
      
    }

    override def toString = "gaps=%s paired=%s suited=%s" format (gaps, paired, suited)
  }
}

class Hand(
    val cards: Hand.Cards,
    val value: List[Card] = List.empty,
    var rank: Option[Rank] = None,
    High: Either[List[Card], Boolean] = Right(false),
    Kicker: Either[List[Card], Boolean] = Right(false)
    ) extends Ordered[Hand] {

  val kicker: List[Card] = Kicker match {
    case Left(cards) => cards
    case Right(true) => cards.value.diff(value) sorted (cards.ordering).reverse take (5 - value.size)
    case Right(false) => List.empty
  }
  
  val high: List[Card] = High match {
    case Left(cards) => cards
    case Right(true) => value sorted (cards.ordering).reverse take (1)
    case Right(false) => List.empty
  }
  
  def ranked(r: Rank) = {
    rank = Some(r)
    Some(this)
  }
  
  def compare(other: Hand): Int = Ranking.compare(this, other)
  
  def equals(other: Hand): Boolean = rank.get == other.rank.get &&
      high == other.high &&
      value == other.value &&
      kicker == other.kicker

  override def toString = "rank=%s high=%s value=%s kicker=%s" format (rank, high, value, kicker)

  def description: String = rank.get match {
    case Rank.High.HighCard ⇒
      "high card %s" format (high.head.kind)

    case Rank.High.OnePair ⇒
      "pair of %ss" format (high.head.kind)

    case Rank.High.TwoPair ⇒
      "two pairs, %ss and %ss" format (high.head.kind, high(1).kind)

    case Rank.High.ThreeKind ⇒
      "three of a kind, %ss" format (high.head.kind)

    case Rank.High.Straight ⇒
      "straight, %s to %s" format (value.min.kind, value.max.kind)

    case Rank.High.Flush ⇒
      "flush, %s high" format (high.head.kind)

    case Rank.High.FullHouse ⇒
      "full house, %ss full of %ss" format (high.head.kind, high(1).kind)

    case Rank.High.FourKind ⇒
      "four of a kind, %ss" format (high.head.kind)

    case Rank.High.StraightFlush ⇒
      "straight flush, %s to %s" format (value.min.kind, value.max.kind)

    case Rank.Badugi.BadugiOne ⇒
      "1-card badugi: %s" format (value head)

    case Rank.Badugi.BadugiTwo ⇒
      "2-card badugi: %s + %s" format (value head, value(1))

    case Rank.Badugi.BadugiThree ⇒
      "3-card badugi: %s + %s + %s" format (value head, value(1), value(2))

    case Rank.Badugi.BadugiFour ⇒
      "4-card badugi: %s + %s + %s + %s" format (value head, value(1), value(2), value(3))
  }
}
