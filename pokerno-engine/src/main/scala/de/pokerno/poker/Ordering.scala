package de.pokerno.poker

private[poker] case object AceHigh extends Ordering[Card] {
  def compare(a: Card, b: Card): Int = a compareTo b
}

private[poker] case object AceLow extends Ordering[Card] {
  def index(card: Card): Int = card.kind match {
    case Kind.Value.Ace ⇒ -1
    case _              ⇒ card.toInt
  }

  def compare(a: Card, b: Card): Int = index(a) compareTo index(b)
}

case object Ranking extends Ordering[Hand] {
  this: Hand ⇒
  private val comparers = List(
    (a: Hand, b: Hand) ⇒ compareRanks(a.rank.get, b.rank.get),
    (a: Hand, b: Hand) ⇒ compareCards(a.high, b.high),
    (a: Hand, b: Hand) ⇒ compareCards(a.value, b.value),
    (a: Hand, b: Hand) ⇒ compareCards(a.kicker, b.kicker)
  )

  def compareRanks(rank1: Rank.Value, rank2: Rank.Value): Int = rank1 compare rank2

  def compareCards(a: List[Card], b: List[Card]): Int = if (a.size == b.size) {
    var result = 0
    a.zipWithIndex.takeWhile {
      case (card, i) ⇒
        result = card compare b(i)
        result == 0
    }
    result
  } else {
    val l = List(a.size, b.size).min
    compareCards(a.take(l), b.take(l))
  }

  def compare(a: Hand, b: Hand): Int = {
    var result = 0
    comparers takeWhile { cmp ⇒
      result = cmp(a, b)
      result == 0
    }
    result
  }

}
