package de.pokerno.poker

import scala.collection.mutable.ListBuffer

class Card(val kind: Kind.Value.Kind, val suit: Suit.Value) extends Ordered[Card] {
  def toInt: Int = (kind.toInt << 2) + suit.toInt
  def toByte: Byte = (toInt + 1) toByte

  override def toString = kind.toString + suit.toString
  def toConsoleString = "%s%s%s%s".format(suit.color, kind.toString, suit.unicode, Console.RESET)
  override def compare(other: Card): Int = {
    kind.toInt compareTo other.kind.toInt
  }
}

object Card {
//  implicit def int2Card(i: Int): Card = Card(i)
//  implicit def str2Card(s: String): Card = Card(s)
  implicit def symbol2Card(s: Symbol): Card = Card(s.name replace("_", ""))

  case class NotACard(value: Any) extends Exception("not a card: %s" format (value))
  case class InvalidCard(value: Any) extends Exception("invalid card: %s" format (value))
  case class ParseError(s: String) extends Exception("can't parse card: %s" format (s))

  private var _all: ListBuffer[Card] = new ListBuffer
  private var _masks: ListBuffer[Int] = new ListBuffer

  final val All = for { kind ← Kind.All; suit ← Suit.All } yield (new Card(kind, suit))
  final val CardsNum = All.size
  final val Masks: List[Int] = for { kind ← Kind.All; suit ← Suit.All } yield (kind.toInt << (1 << 4 * suit.toInt))
  final val Seq = List range (0, 51)

  @throws[NotACard]
  def apply(value: Any): Card = value match {
    case i: Byte => parseInt(i - 1)
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

  def wrap(i: Byte) = All(i - 1)
  def wrap(i: Int) = All(i)
  def wrap(kind: Kind.Value.Kind, suit: Suit.Value) = All((kind.toInt << 2) + suit.toInt)
}

object Cards {
  implicit def list2Cards(c: List[Card]): Cards = new Cards(c)
  implicit def cards2List(c: Cards): List[Card] = c.value
  implicit def cards2String(c: Cards): String = c

  def apply(value: String) = parseString(value)
  def apply(value: List[_]): Cards = parseList(value)

  def parseList(l: List[_]): List[Card] = l.map(Card(_))

  def parseString(s: String): List[Card] = {
    val regex = """(?i)([akqjt2-9]{1})([shdc]{1})""".r
    val matching = for {
      regex(kind, suit) ← regex findAllIn s
    } yield Card.wrap(kind(0), suit(0))
    matching.toList
  }

  val OrderingByHead = Ordering.by[List[Card], Card](_ head)
  val OrderingByMax = Ordering.by[List[Card], Card](_ max)
}

class Cards(val value: List[Card]) {
  override def toString = value.map(_ toString) mkString ("")
  def toConsoleString = value.map(_ toConsoleString) mkString ("")
}
