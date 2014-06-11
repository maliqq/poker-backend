package de.pokerno.poker

import collection.mutable.ListBuffer

object Card {
  case class Invalid(value: Any)      extends Exception("invalid card: %s (%s)".format(value, value.getClass.getName))
  case class ParseError(s: String)    extends Exception("can't parse card: %s" format s)

  private var _all: ListBuffer[Card] = new ListBuffer
  private var _masks: ListBuffer[Int] = new ListBuffer

  final val CardsNum = All.size
  final val Masks: Seq[Int] = for {
    kind ← Kinds;
    suit ← Suits
  } yield kind.toInt << (1 << 4 * suit.toInt)
  final val Seq = List range (0, 51)

  implicit def fromSymbol(s: Symbol): Card = fromString(s.name replace ("_", ""))
  implicit def fromByte(b: Byte): Card = fromInt(b - 1)

  @throws[Invalid]
  implicit def fromInt(i: Int): Card = {
    if (i < 0 || i >= CardsNum) throw Invalid(i)
    apply(i)
  }

  @throws[ParseError]
  implicit def fromString(s: String): Card = {
    if (s.size != 2) throw ParseError(s)
    try {
      return apply(s.head, s.last)
    } catch {
      case _: java.lang.IndexOutOfBoundsException =>
        throw ParseError(s)
    }
  }
  
  def apply(kind: Kind.Value.Kind, suit: Suit.Value) = All((kind.toInt << 2) + suit.toInt)

  def apply(c: Any): Card = c match {
    case s: String => fromString(s)
    case i: Byte => All(i - 1)
    case i: Int => All(i)
    case _ => throw Invalid(c)
  }
  
}

class Card(val kind: Kind.Value.Kind, val suit: Suit.Value) extends Ordered[Card] {
  def toInt: Int = (kind.toInt << 2) + suit.toInt
  def toByte: Byte = (toInt + 1).toByte

  override def toString = kind.toString + suit.toString
  override def compare(other: Card): Int = {
    kind.toInt compareTo other.kind.toInt
  }

  def toColoredString = suit.color + kind.toString + suit.unicode + Console.RESET
}
