package de.pokerno.poker

private[poker] trait Low { _: CardSet ⇒

  private[poker] def isLow: Option[Hand] = {
    val uniq: Cards = groupKind.values.map(_(0)).toList
    val lowCards = uniq.reverse take 5
    if (lowCards.size == 0)
      return None

    val h = hand(
      value = lowCards,
      high = Left(List(lowCards max)))
    val rankType = if (lowCards.size == 5) Rank.Low.Complete else Rank.Low.Incomplete

    h.map(_.ranked(rankType))
  }

  def isGapLow: Option[Hand] = {
    val hand = (new CardSet(value) with HighHand).isHigh
    if (hand.get.rank == Rank.High.HighCard) isLow
    else hand
  }
  
}
