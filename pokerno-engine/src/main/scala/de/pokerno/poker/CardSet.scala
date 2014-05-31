package de.pokerno.poker

private[poker] class CardSet(val value: Seq[Card], val ordering: Ordering[Card] = AceHigh) {
  lazy val gaps = groupByGaps
  lazy val groupKind: Map[Kind.Value.Kind, Seq[Card]] = value.groupBy(_.kind)
  lazy val groupSuit: Map[Suit.Value, Seq[Card]] = value.groupBy(_.suit)
  lazy val paired = countGroups(groupKind)
  lazy val suited = countGroups(groupSuit)

  private def countGroups(groups: Map[_ <: Any, Seq[Card]]): Map[Int, Seq[Seq[Card]]] = {
    var _counter: Map[Int, Seq[Seq[Card]]] = Map.empty
    groups foreach {
      case (k, v) ⇒
        val count = v.size
        val group = _counter getOrElse (count, List.empty)
        _counter += (count -> (group ++ List(v)))
    }
    _counter
  }

  private def groupByGaps: Seq[Seq[Card]] = {
    var _gaps = Seq[Seq[Card]]()
    val cards = value.filter(_.kind == Kind.Value.Ace) ++ value.sorted(AceHigh)
    val (_, _buffer: Seq[Card]) = cards.foldLeft((cards.head, Seq[Card]())) {
      case ((prev: Card, buffer: Seq[Card]), card) ⇒
        lazy val d = card.kind.toInt - prev.kind.toInt
        if (card == prev || d == 1 || d == -12)
          (card, buffer ++ Seq(card))
        else if (d == 0)
          (card, buffer)
        else {
          _gaps ++= Seq(buffer)
          (card, Seq[Card](card))
        }
    }
    _gaps ++ Seq(_buffer)
  }

  override def toString = "gaps=%s paired=%s suited=%s" format (gaps, paired, suited)
}

private[poker] class CardSetQualifier(
    _cards: Seq[Card],
    _ordering: Ordering[Card] = AceHigh) {

  def qualify(q: Kind.Value.Kind) =
    new CardSet(
      _cards filter { card ⇒ card.kind.toInt <= q.toInt },
      _ordering
    )

  def kick(_cards: Seq[Card]) =
    new CardSet(
      _cards.diff(_cards).sorted(_ordering) take (5 - _cards.size),
      _ordering
    )

}
