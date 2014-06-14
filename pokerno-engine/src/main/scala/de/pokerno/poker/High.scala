package de.pokerno.poker

private[poker] trait HighHand { self: CardSet ⇒

  import Rank.High._
  
  def isStraightFlush: Option[Hand] = isFlush map { flush =>
    val cards = new CardSet(flush.value) with HighHand
    cards.isStraight map { _ =>
      flush.ranked(StraightFlush)
    } orElse isFourKind orElse isFullHouse orElse Some(flush)
  } getOrElse isFourKind orElse isFullHouse orElse isStraight

  def isFourKind: Option[Hand] = paired get 4 map { quads =>
    val cards = quads.head
    hand(
        value   = cards,
        high    = Left(cards),
        kicker  = Right(true)
      ).map(_.ranked(FourKind))
  } getOrElse(None)

  def isFullHouse: Option[Hand] = paired get 3 map { sets =>
    val (major, minor) = if (sets.size > 1) {
      val Seq(minor, major, _*) = sets.sorted(CardOrdering.ByHead).reverse.take(2)
      (major, minor)
    } else {
      paired.get(2) match {
        case None ⇒ return None
        case Some(pairs) ⇒
          val minor = pairs.sorted(CardOrdering.ByHead).head
          val major = sets.head
          (major, minor)
      }
    }
    hand(
        value   = major ++ minor,
        high    = Left(List(major head, minor head))
      ).map(_.ranked(FullHouse))
  } getOrElse(None)

  def isFlush: Option[Hand] = suited find { _._1 >= 5 } map { group =>
    val cards = group._2.head.sorted.reverse
    hand(
        value   = cards.take(5),
        high    = Left(cards.take(1))
      ).map(_.ranked(Flush))
  } getOrElse(None)

  def isStraight: Option[Hand] = gaps find { _.size >= 5 } map { group => // FIXME sorted for Ace low
    val cards = group.reverse
    hand(
        value   = cards take 5,
        high    = Left(cards take 1)
      ).map(_.ranked(Straight))
  } getOrElse(None)

  def isThreeKind: Option[Hand] = paired get 3 map { sets =>
    if (sets.size == 1)
      hand(
          value   = sets head,
          high    = Right(true),
          kicker  = Right(true)
        ).map(_.ranked(ThreeKind))
    else None
  } getOrElse(None)

  def isTwoPair: Option[Hand] = paired get 2 map { pairs =>
    if (pairs.size >= 2) {
      val Seq(major, minor, _*) = pairs.sorted(CardOrdering.ByMax).reverse
      hand(
          value   = major ++ minor,
          high    = Left(List(major head, minor head)),
          kicker  = Right(true)).map(_.ranked(TwoPair))
    } else None
  } getOrElse(None)

  def isOnePair: Option[Hand] = paired get 2 map { pairs =>
    if (pairs.size == 1)
      hand(
          value   = pairs head,
          high    = Right(true),
          kicker  = Right(true)
        ).map(_.ranked(OnePair))
    else None
  } getOrElse(None)

  def isHighCard: Option[Hand] = {
    val highest = value.max(ordering)
    hand(
        value     = List(highest),
        high      = Right(true),
        kicker    = Right(true)
      ).map(_.ranked(HighCard))
  }

  @throws[Hand.InvalidCards]
  def isHigh: Option[Hand] = {
    if (value.size < 5 || value.size > 7)
      throw Hand.InvalidCards("5, 6 or 7 cards required to detect high hand")

    isStraightFlush orElse isThreeKind orElse isTwoPair orElse isOnePair orElse isHighCard
  }
}
