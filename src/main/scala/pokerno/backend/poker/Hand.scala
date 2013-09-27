package pokerno.backend.poker

object Hand {
  sealed trait Ranking
  
  abstract class HighRanking extends Ranking
  case object High extends HighRanking
  
  abstract class LowRanking extends Ranking
  case object AceFive extends LowRanking
  case object AceFive8 extends LowRanking
  case object AceSix extends LowRanking
  case object DeuceSix extends LowRanking
  case object DeuceSeven extends LowRanking

  abstract class BadugiRanking extends Ranking
  case object BadugiOne extends BadugiRanking
  case object BadugiTwo extends BadugiRanking
  case object BadugiThree extends BadugiRanking
  case object BadigiFour extends BadugiRanking
  
  class Cards(val value: List[Card]) {
    lazy val gaps = groupByGaps
    lazy val groupKind: Map[Kind.Value, List[Card]] = value.groupBy(_.kind)
    lazy val groupSuit: Map[Suit.Value, List[Card]] = value.groupBy(_.suit)
    lazy val paired = countGroups(groupKind)
    lazy val suited = countGroups(groupSuit)
    
    private def countGroups(groups: Map[_ <: Any, List[Card]]): Map[Int, List[List[Card]]] = {
      var result: Map[Int, List[List[Card]]] = Map.empty
      groups foreach { case (k, v) =>
        val count = v.size
        val group = result.getOrElse(count, List.empty)
        result += (count -> (group ++ List(v)))
      }
      result
    }
    
    private def groupByGaps: List[List[Card]] = {
      var result: List[List[Card]] = List[List[Card]]()
      val first = value.head
      value.foldRight((first, List[Card]())) { case (card, (prev: Card, buffer: List[Card])) =>
        val d = card.toInt - prev.toInt
        if (d == 0)
          (prev, buffer)
        else if (d == 1 || d == -12)
          (prev, buffer ++ List(card))
        else {
          result ::= buffer
          (card, List[Card]())
        }
      }
      result
    }
    
    override def toString = "gaps=%s paired=%s suited=%s".format(gaps, paired, suited)
  }
}

class Hand(
    val cards: Hand.Cards = new Hand.Cards(List.empty),
    val value: List[Card] = List.empty,
    var ranking: Option[Hand.Ranking] = None,
    var high: List[Card] = List.empty,
    var kicker: List[Card] = List.empty,
    _rank: Boolean = false,
    _high: Boolean = false,
    _kicker: Boolean = false
  ) {
  
  if (_kicker && kicker.isEmpty)
    kicker = cards.kick(value)
  
  override def toString = "ranking=%s high=%s value=%s kicker=%s".format(ranking, high, value, kicker)
}

def (h *Hand) PrintString() string {
  switch h.Rank {
  case hand.HighCard =>
    return fmt.Sprintf("high card %s",
      h.High[0].KindTitle(),
    )

  case hand.OnePair =>
    return fmt.Sprintf("pair of %ss",
      h.High[0].KindTitle(),
    )

  case hand.TwoPair =>
    return fmt.Sprintf("two pairs, %ss and %ss",
      h.High[0].KindTitle(),
      h.High[1].KindTitle(),
    )

  case hand.ThreeKind =>
    return fmt.Sprintf("three of a kind, %ss",
      h.High[0].KindTitle(),
    )

  case hand.Straight =>
    return fmt.Sprintf("straight, %s to %s",
      h.Value.Min(AceHigh).KindTitle(),
      h.Value.Max(AceHigh).KindTitle(),
    )

  case hand.Flush =>
    return fmt.Sprintf("flush, %s high",
      h.High[0].KindTitle(),
    )

  case hand.FullHouse =>
    return fmt.Sprintf("full house, %ss full of %ss",
      h.High[0].KindTitle(),
      h.High[1].KindTitle(),
    )

  case hand.FourKind =>
    return fmt.Sprintf("four of a kind, %ss",
      h.High[0].KindTitle(),
    )

  case hand.StraightFlush =>
    return fmt.Sprintf("straight flush, %s to %s",
      h.Value.Min(AceHigh).KindTitle(),
      h.Value.Max(AceHigh).KindTitle(),
    )

  case hand.BadugiOne =>
    return fmt.Sprintf("1-card badugi: %s",
      h.Value[0].KindTitle(),
    )

  case hand.BadugiTwo =>
    return fmt.Sprintf("2-card badugi: %s + %s",
      h.Value[0].KindTitle(),
      h.Value[1].KindTitle(),
    )

  case hand.BadugiThree =>
    return fmt.Sprintf("3-card badugi: %s + %s + %s",
      h.Value[0].KindTitle(),
      h.Value[1].KindTitle(),
      h.Value[2].KindTitle(),
    )

  case hand.BadugiFour =>
    return fmt.Sprintf("4-card badugi: %s + %s + %s + %s",
      h.Value[0].KindTitle(),
      h.Value[1].KindTitle(),
      h.Value[2].KindTitle(),
      h.Value[3].KindTitle(),
    )
  }

  return ""
}
}

def (hc *handCards) Detect(ranks []rankFunc) *Hand {
  var result *Hand

  for _, val r = range ranks {
    rank, val hand = r(hc)

    if (hand != nil) {
      if (!hand.rank) {
        hand.Rank = rank
      }
      if (hand.high) {
        hand.High = Cards{hand.Value[0]}
      }
      if (hand.kicker) {
        hand.Kicker = hc.cardsHelper.Kickers(hand.Value)
      }

      hand.handCards = hc

      result = hand

      break
    }
  }

  return result
}


type compareFunc func(*Hand, *Hand) int

var compareWith = func(ord Ordering) []compareFunc {
  return []compareFunc{
    func(a *Hand, b *Hand) int {
      return a.Rank.Compare(b.Rank)
    },

    func(a *Hand, b *Hand) int {
      return a.High.Compare(b.High, ord)
    },

    func(a *Hand, b *Hand) int {
      return a.Value.Compare(b.Value, ord)
    },

    func(a *Hand, b *Hand) int {
      return a.Kicker.Compare(b.Kicker, ord)
    }
  }
}

def (h *Hand) Compare(o *Hand) int {
  val ord = h.handCards.Ordering

  for _, val compare = range compareWith(ord) {
    val result = compare(h, o)
    if (result != 0) {
      return result
    }
  }

  return 0
}
