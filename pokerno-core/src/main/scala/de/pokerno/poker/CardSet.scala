package de.pokerno.poker

private[poker] class CardSet(val value: List[Card], val ordering: Ordering[Card] = AceHigh) {
  lazy val gaps = groupByGaps
  lazy val groupKind: Map[Kind.Value.Kind, List[Card]] = value.groupBy(_.kind)
  lazy val groupSuit: Map[Suit.Value, List[Card]] = value.groupBy(_.suit)
  lazy val paired = countGroups(groupKind)
  lazy val suited = countGroups(groupSuit)

  private def countGroups(groups: Map[_ <: Any, List[Card]]): Map[Int, List[List[Card]]] = {
    var _counter: Map[Int, List[List[Card]]] = Map.empty
    groups foreach {
      case (k, v) ⇒
        val count = v.size
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
  
  override def toString = "gaps=%s paired=%s suited=%s" format (gaps, paired, suited)
}

private[poker] class CardSetQualifier(
    _cards: List[Card],
    _ordering: Ordering[Card] = AceHigh) {
  
  def qualify(q: Kind.Value.Kind) =
    new CardSet(
        _cards filter { card ⇒ card.kind.toInt <= q.toInt },
        _ordering
      )

  def kick(_cards: List[Card]) =
    new CardSet(
        _cards.diff(_cards).sorted(_ordering) take (5 - _cards.size),
        _ordering
      )
  
}
