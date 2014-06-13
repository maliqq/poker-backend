package de.pokerno.poker

private[poker] class CardSet(val value: Cards, val ordering: Ordering[Card] = AceHigh) {
  lazy val gaps = groupByGaps
  lazy val groupKind: Map[Kind.Value.Kind, Cards] = value.groupBy(_.kind)
  lazy val groupSuit: Map[Suit.Value, Cards] = value.groupBy(_.suit)
  lazy val paired = countGroups(groupKind)
  lazy val suited = countGroups(groupSuit)

  private def countGroups(groups: Map[_ <: Any, Cards]): Map[Int, Seq[Cards]] = {
    groups.foldLeft(Map[Int, Seq[Cards]]()) { case (_counter, (k, v)) =>
      val count = v.size
      val group = _counter getOrElse (count, Seq.empty)
      _counter + (count -> (group ++ Seq(v)))
    }
  }

  private def groupByGaps: Seq[Cards] = {
    var _gaps = Seq[Cards]()
    val cards = value.filter(_.kind == Kind.Value.Ace) ++ value.sorted(AceHigh)
    val (_, _buffer: Cards) = cards.foldLeft((cards.head, Cards.empty)) {
      case ((prev: Card, buffer: Cards), card) ⇒
        lazy val d = card.kind.toInt - prev.kind.toInt
        if (card == prev || d == 1 || d == -12)
          (card, buffer ++ Seq(card))
        else if (d == 0)
          (card, buffer)
        else {
          _gaps ++= Seq(buffer)
          (card, Seq(card))
        }
    }
    _gaps ++ Seq(_buffer)
  }
  
  def hand(value: Cards = Seq.empty, rank: Option[Rank.Value] = None,
      high: Either[Cards, Boolean] = Right(false),
      kicker: Either[Cards, Boolean] = Right(false)) =
    new Hand(this, value, rank, high, kicker)

  override def toString = "gaps=%s paired=%s suited=%s" format (gaps, paired, suited)
}

private[poker] class CardSetQualifier(
    _cards: Cards,
    _ordering: Ordering[Card] = AceHigh) {

  def qualify(q: Kind.Value.Kind) =
    new CardSet(
      _cards filter { card ⇒ card.kind.toInt <= q.toInt },
      _ordering
    )

  def kick(_cards: Cards) =
    new CardSet(
      _cards.diff(_cards).sorted(_ordering) take (5 - _cards.size),
      _ordering
    )

}
