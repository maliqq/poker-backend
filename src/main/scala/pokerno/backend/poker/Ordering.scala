package pokerno.backend.poker

case object AceHigh extends Ordering[Card] {
  def compare(a: Card, b: Card): Int = {
    a compareTo b
  }
}

case object AceLow extends Ordering[Card] {
  def compare(a: Card, b: Card): Int = {
    a compareTo b
  }
}
