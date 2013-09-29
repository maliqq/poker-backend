package pokerno.backend.poker

class Low {
self: Hand.Cards =>
  def isLow: Option[Hand] = {
    val uniq: List[Card] = groupKind.values.map(_(0)).toList
    val lowCards = uniq.reverse.take(5)
    if (lowCards.size == 0)
      return None

    var hand = new Hand(value = lowCards, high = List(lowCards.max))
    if (lowCards.size == 5)
      hand ranked Rank.CompleteLow
    else
      hand ranked Rank.IncompleteLow
  }
  
  def isGapLow: Option[Hand] = {
    val hand = (new Hand.Cards(value) with HighHand).isHigh
    if (hand.get.rank == Rank.HighCard)
      isLow
    else
      hand
  }
}
