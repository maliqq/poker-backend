package pokerno.backend.poker

trait High extends Hand {
  val noFlushRanks = ()
  val noStraightRanks = ()
  
  def isStraightFlush: Option[Hand] = {
    val maybeFlush = isFlush
    if (maybeFlush.isEmpty) {
      val hand = Hand.Detect(noFlushRanks)
      if (hand.isDefined)
        hand.get._rank = true
      return hand
    }

    val flushCards = maybeFlush.get.value
    val newhc = NewHandCards(&flushCards, cards.Ordering, false)

    if (val maybeStraight = newcards.isStraight; maybeStraight != nil) {
      return maybeStraight
    }

    val maybeHigher = cards.Detect(noStraightRanks)

    if (maybeHigher != nil) {
      maybeHigher.rank = true
      return maybeHigher
    }

    val justFlush = *maybeFlush

    justFlush.rank = true
    justFlush.Rank = hand.Flush

    return &justFlush
  }
  
  def isFourKind: Option[Hand] = cards.paired.get(4) match {
    case Some(quads) =>
      val cards = quads.head
      Some(new Hand(value = cards, high = cards, _kicker = true))
    case None => None
  }

  def isFullHouse: Option[Hand] = cards.paired.get(3) match {
    case Some(sets) =>
      val (major, minor) = if (sets.size > 1) {
        val List(minor, major, _*) = sets.sorted(Cards.OrderingByHead).take(2)
        (major, minor)
      } else {
        val pairs = cards.paired.get(2)
        if (pairs.isEmpty)
          return None
        val minor = pairs.get.sorted(Cards.OrderingByHead).head
        val major = sets.head
        (major, minor)
      }
      Some(new Hand(value = major ++ minor, high = List(major.head, minor.head)))
    case None => None
  }

  def isFlush: Option[Hand] = {
    val group = cards.suited.find { case (count, group) => count >= 5 }
    if (group.isDefined) {
      val cards = group.get._2.head.sorted
      return Some(new Hand(value = cards.take(5), high = cards.take(1)))
    }
    return None
  }
  
  def isStraight: Option[Hand] = {
    val group = cards.gaps.find { group => group.size > 5 }
    if (group.isDefined) {
      val cards = group.get.sorted
      return Some(new Hand(value = cards.take(5), high = cards.take(1)))
    }
    return None
  }

  def isThreeKind: Option[Hand] = cards.paired.get(3) match {
    case Some(sets) =>
      Some(new Hand(value = sets.head, _high = true, _kicker = true))
    case None => None
  }

  def isTwoPair: Option[Hand] = cards.paired.get(2) match {
    case Some(pairs) =>
      val List(major, minor, _*) = pairs.sorted(Cards.OrderingByMax)
      Some(new Hand(value = major ++ minor, high = List(major.head, minor.head), _kicker = true))
    case None => None
  }

  def isOnePair: Option[Hand] = cards.paired.get(2) match {
    case Some(pairs) =>
      Some(new Hand(value = pairs.head, _high = true, _kicker = true))
    case None => None
  }

  def isHighCard: Option[Hand] = Some(new Hand(value = cards.value.sorted, _high = true, _kicker = true))

  def isHigh(_cards: List[Card]): Option[Hand] = {
    if (_cards.size < 5)
      throw new Error("5 or more cards required to detect high hand")
    
    val cardSet = new CardSet(_cards, ordering = AceHigh)
    cardSet.detectWith(highRanks)
  }
}
