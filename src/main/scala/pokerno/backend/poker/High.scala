package pokerno.backend.poker

trait HighHand {
self: Hand.Cards =>
  def isStraightFlush: Option[Hand] = isFlush match {
    case None => isFourKind orElse isFullHouse orElse isStraight
    case Some(flush) =>
      Hand.High(flush.value) match {
        case Some(hand) => hand ranked Rank.StraightFlush
        case None => isFourKind orElse isFullHouse orElse Some(flush)
      }
  }
  
  def isFourKind: Option[Hand] = paired.get(4) match {
    case Some(quads) =>
      val cards = quads.head
      new Hand(value = cards, high = cards, _kicker = true) ranked Rank.FourKind
    case None => None
  }

  def isFullHouse: Option[Hand] = paired.get(3) match {
    case Some(sets) =>
      val (major, minor) = if (sets.size > 1) {
        val List(minor, major, _*) = sets.sorted(Cards.OrderingByHead).take(2)
        (major, minor)
      } else {
        paired.get(2) match {
          case None => return None
          case Some(pairs) =>
            val minor = pairs.sorted(Cards.OrderingByHead).head
            val major = sets.head
            (major, minor)
        }
      }
      new Hand(value = major ++ minor, high = List(major.head, minor.head)) ranked Rank.FullHouse
    case None => None
  }

  def isFlush: Option[Hand] = suited.find { case (count, group) => count >= 5 } match {
    case Some(group) =>
      val cards = group._2.head.sorted
      new Hand(value = value.take(5), high = value.take(1)) ranked Rank.Flush
    case None => None
  }
  
  def isStraight: Option[Hand] = gaps.find { group => group.size > 5 } match {
    case Some(group) =>
      val cards = group.sorted
      new Hand(value = value.take(5), high = value.take(1)) ranked Rank.Straight
    case None => None
  }

  def isThreeKind: Option[Hand] = paired.get(3) match {
    case Some(sets) =>
      new Hand(value = sets.head, _high = true, _kicker = true) ranked Rank.ThreeKind
    case None => None
  }

  def isTwoPair: Option[Hand] = paired.get(2) match {
    case Some(pairs) =>
      val List(major, minor, _*) = pairs.sorted(Cards.OrderingByMax)
      new Hand(value = major ++ minor, high = List(major.head, minor.head), _kicker = true) ranked Rank.TwoPair
    case None => None
  }

  def isOnePair: Option[Hand] = paired.get(2) match {
    case Some(pairs) =>
      new Hand(value = pairs.head, _high = true, _kicker = true) ranked Rank.OnePair
    case None => None
  }

  def isHighCard: Option[Hand] = new Hand(value = value.sorted, _high = true, _kicker = true) ranked Rank.HighCard

  def isHigh: Option[Hand] = {
    if (value.size < 5)
      throw new Error("5 or more cards required to detect high hand")
    
    isStraightFlush orElse isThreeKind orElse isTwoPair orElse isOnePair orElse isHighCard
  }
}
