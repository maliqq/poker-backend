package de.pokerno.poker

object CardUtils {
  def parseList(l: Seq[_]): Seq[Card] = l.map(Card(_))

  def parseString(s: String): Seq[Card] = {
    val regex = """(?i)([akqjt2-9]{1})([shdc]{1})""".r
    val matching = for {
      regex(kind, suit) ← regex findAllIn s
    } yield Card.wrap(kind(0), suit(0))
    matching.toList
  }
}
