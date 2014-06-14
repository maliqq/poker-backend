package de.pokerno.poker

object CardOrdering {
  final val ByHead = Ordering.by[Cards, Card](_ head)
  final val ByMax = Ordering.by[Cards, Card](_ max)
}

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

case object Ranking extends Ordering[Hand] with Comparing { h: Hand ⇒
  
  private val comparers = Seq(
    (a: Hand, b: Hand) ⇒ compareRanks(a.rank, b.rank),
    (a: Hand, b: Hand) ⇒ compareCards(a.high, b.high),
    (a: Hand, b: Hand) ⇒ compareCards(a.value, b.value),
    (a: Hand, b: Hand) ⇒ compareCards(a.kicker, b.kicker)
  )

  def compare(a: Hand, b: Hand): Int = {
    var result = 0
    comparers takeWhile { cmp ⇒
      result = cmp(a, b)
      result == 0
    }
    result
  }

}
