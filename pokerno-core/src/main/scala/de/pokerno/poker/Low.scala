package de.pokerno.poker

class Low {
  
  self: Hand.Cards ⇒

  def isLow: Option[Hand] = {
    val uniq: List[Card] = groupKind.values.map(_(0)).toList
    val lowCards = uniq.reverse take 5
    if (lowCards.size == 0)
      return None

    val hand = new Hand(self,
      value = lowCards,
      High = Left(List(lowCards max)))
    val rankType = if (lowCards.size == 5) Rank.Low.Complete else Rank.Low.Incomplete

    hand ranked rankType
  }

  def isGapLow: Option[Hand] = {
    val hand = (new Hand.Cards(value) with HighHand).isHigh
    if (hand.get.rank == Rank.High.HighCard) isLow
    else hand
  }
}
