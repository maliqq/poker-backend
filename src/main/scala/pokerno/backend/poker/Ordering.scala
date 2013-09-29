package pokerno.backend.poker

case object AceHigh extends Ordering[Card] {
  def compare(a: Card, b: Card): Int = -1
}

case object AceLow extends Ordering[Card] {
  def compare(a: Card, b: Card): Int = -1
}
