package de.pokerno.poker

import collection.mutable.ListBuffer

class Card(val kind: Kind.Value.Kind, val suit: Suit.Value) extends Ordered[Card] {
  def toInt: Int = (kind.toInt << 2) + suit.toInt
  def toByte: Byte = (toInt + 1).toByte

  override def toString = kind.toString + suit.toString
  def toConsoleString = "%s%s%s%s".format(suit.color, kind.toString, suit.unicode, Console.RESET)
  override def compare(other: Card): Int = {
    kind.toInt compareTo other.kind.toInt
  }
}

object Card {
  //  implicit def int2Card(i: Int): Card = Card(i)
  //  implicit def str2Card(s: String): Card = Card(s)
  implicit def symbol2Card(s: Symbol): Card = Card(s.name replace ("_", ""))

  case class NotACard(value: Any) extends Exception("not a card: %s" format value)
  case class InvalidCard(value: Any) extends Exception("invalid card: %s" format value)
  case class ParseError(s: String) extends Exception("can't parse card: %s" format s)

  private var _all: ListBuffer[Card] = new ListBuffer
  private var _masks: ListBuffer[Int] = new ListBuffer

  final val CardsNum = Cards.size
  final val Masks: List[Int] = for {
    kind ← Kinds;
    suit ← Suits
  } yield kind.toInt << (1 << 4 * suit.toInt)
  final val Seq = List range (0, 51)

  @throws[NotACard]
  def apply(value: Any): Card = value match {
    case i: Byte   ⇒ parseInt(i - 1)
    case i: Int    ⇒ parseInt(i)
    case s: String ⇒ parseString(s)
    case c: Card   ⇒ c
    case _         ⇒ throw NotACard()
  }

  @throws[InvalidCard]
  implicit def parseInt(i: Int): Card = {
    if (i < 0 || i >= CardsNum) throw InvalidCard(i)
    wrap(i)
  }

  @throws[ParseError]
  implicit def parseString(s: String): Card = {
    if (s.size != 2) throw ParseError(s)
    val List(kind, suit) = s.toList
    wrap(kind, suit)
  }

  def wrap(i: Byte) = Cards(i - 1)
  def wrap(i: Int) = Cards(i)
  def wrap(kind: Kind.Value.Kind, suit: Suit.Value) = Cards((kind.toInt << 2) + suit.toInt)
}
