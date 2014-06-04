package de.pokerno.poker

object Cards {

  def empty: Cards = Seq.empty

  def fromBinary(b: Array[Byte]): Cards = Array(b).map(Card(_))
  def fromSeq(l: Seq[_]): Cards = l.map(Card(_))

  def fromString(s: String): Cards = {
    val regex = """(?i)([akqjt2-9]{1})([shdc]{1})""".r
    val matching = for {
      regex(kind, suit) ← regex findAllIn s
    } yield Card.wrap(kind(0), suit(0))
    matching.toList
  }

}
