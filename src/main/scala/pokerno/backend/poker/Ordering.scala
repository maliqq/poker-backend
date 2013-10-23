package pokerno.backend.poker

case object AceHigh extends Ordering[Card] {
  def compare(a: Card, b: Card): Int = a compareTo b
}

case object AceLow extends Ordering[Card] {
  def index(card: Card): Int = card.kind match {
    case Kind.Value.Ace ⇒ -1
    case _              ⇒ card.toInt
  }

  def compare(a: Card, b: Card): Int = index(a) compareTo index(b)
}
