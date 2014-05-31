package de.pokerno.poker

object CardUtils {
  implicit def list2Cards(c: Seq[Card]): CardsWrapper = CardsWrapper(c)
  implicit def cards2List(c: CardsWrapper): Seq[Card] = c.value
  implicit def cards2String(c: CardsWrapper): String = c

  def parseList(l: Seq[_]): Seq[Card] = l.map(Card(_))

  def parseString(s: String): Seq[Card] = {
    val regex = """(?i)([akqjt2-9]{1})([shdc]{1})""".r
    val matching = for {
      regex(kind, suit) ← regex findAllIn s
    } yield Card.wrap(kind(0), suit(0))
    matching.toList
  }
}

case class CardsWrapper(val value: Seq[Card]) {
  override def toString = value.map(_ toString) mkString ""
  def toConsoleString = value.map(_ toConsoleString) mkString ""
}
