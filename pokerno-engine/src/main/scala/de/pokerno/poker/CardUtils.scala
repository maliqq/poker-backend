package de.pokerno.poker

object CardUtils {
  implicit def list2Cards(c: List[Card]): CardsWrapper = CardsWrapper(c)
  implicit def cards2List(c: CardsWrapper): List[Card] = c.value
  implicit def cards2String(c: CardsWrapper): String = c

  def parseList(l: List[_]): List[Card] = l.map(Card(_))

  def parseString(s: String): List[Card] = {
    val regex = """(?i)([akqjt2-9]{1})([shdc]{1})""".r
    val matching = for {
      regex(kind, suit) ← regex findAllIn s
    } yield Card.wrap(kind(0), suit(0))
    matching.toList
  }
}

case class CardsWrapper(val value: List[Card]) {
  override def toString = value.map(_ toString) mkString ""
  def toConsoleString = value.map(_ toConsoleString) mkString ""
}
