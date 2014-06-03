package de.pokerno.poker

private[poker] trait HighHand {

  self: CardSet ⇒

  def isStraightFlush: Option[Hand] = isFlush match {
    case None ⇒ isFourKind orElse isFullHouse orElse isStraight
    case Some(flush) ⇒
      val cards = new CardSet(flush.value) with HighHand
      cards.isStraight match {
        case Some(hand) ⇒ flush ranked Rank.High.StraightFlush
        case None       ⇒ isFourKind orElse isFullHouse orElse Some(flush)
      }
  }

  def isFourKind: Option[Hand] = paired get 4 match {
    case Some(quads) ⇒
      val cards = quads.head
      hand(value = cards, high = Left(cards), kicker = Right(true)) ranked Rank.High.FourKind
    case None ⇒ None
  }

  def isFullHouse: Option[Hand] = paired get 3 match {
    case Some(sets) ⇒
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
      hand(value = major ++ minor, high = Left(List(major head, minor head))) ranked Rank.High.FullHouse
    case None ⇒ None
  }

  def isFlush: Option[Hand] = suited.find { case (count, group) ⇒ count >= 5 } match {
    case Some(group) ⇒
      val cards = group._2.head.sorted.reverse
      hand(value = cards.take(5), high = Left(cards.take(1))) ranked Rank.High.Flush
    case None ⇒ None
  }

  def isStraight: Option[Hand] = gaps find { group ⇒ group.size >= 5 } match { // FIXME sorted for Ace low
    case Some(group) ⇒
      val cards = group.reverse
      hand(value = cards take 5, high = Left(cards take 1)) ranked Rank.High.Straight
    case None ⇒ None
  }

  def isThreeKind: Option[Hand] = paired get 3 match {
    case Some(sets) if sets.size == 1 ⇒
      hand(value = sets head, high = Right(true), kicker = Right(true)) ranked Rank.High.ThreeKind
    case None ⇒ None
    case _    ⇒ None
  }

  def isTwoPair: Option[Hand] = paired get 2 match {
    case Some(pairs) if pairs.size >= 2 ⇒
      val Seq(major, minor, _*) = pairs.sorted(CardOrdering.ByMax).reverse
      hand(value = major ++ minor, high = Left(List(major head, minor head)), kicker = Right(true)) ranked Rank.High.TwoPair
    case None ⇒ None
    case _    ⇒ None
  }

  def isOnePair: Option[Hand] = paired get 2 match {
    case Some(pairs) if pairs.size == 1 ⇒
      hand(value = pairs head, high = Right(true), kicker = Right(true)) ranked Rank.High.OnePair
    case None ⇒ None
  }

  def isHighCard: Option[Hand] = {
    val highest = value.max(ordering)
    hand(value = List(highest), high = Right(true), kicker = Right(true)) ranked Rank.High.HighCard
  }

  @throws[Hand.InvalidCards]
  def isHigh: Option[Hand] = {
    if (value.size < 5 || value.size > 7) throw Hand.InvalidCards("5, 6 or 7 cards required to detect high hand")

    isStraightFlush orElse isThreeKind orElse isTwoPair orElse isOnePair orElse isHighCard
  }
}
