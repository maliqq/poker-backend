package pokerno.backend.poker

class CardSet(
    var cards: List[Card],
    val ordering: Ordering[Card] = AceHigh,
    qualifier: Option[Kind.Value] = None
) {
  if (qualifier.isDefined)
    cards = qualify(qualifier get)
  
  def qualify(q: Kind.Value): List[Card] = {
    cards filter { card => card.kind.toInt <= q.toInt }
  }
  
  def kick(_cards: List[Card]): List[Card] = {
    cards.diff(_cards).sorted take(5 - _cards.size)
  }
}
