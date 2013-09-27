package pokerno.backend.poker

import scala.collection.mutable.ListBuffer

class Card(val kind: Kind.Value, val suit: Suit.Value) extends Comparable[Card] {
  def colored = "%s%s %s%s".format(suit.color, kind.toString, suit.toString, Console.RESET)
  def toInt: Int = kind.toInt << 2 + suit.toInt
  def toByte: Byte = (toInt + 1).toByte
  override def toString = kind.toString + suit.toString
  override def equals(other: Card): Boolean = kind == other.kind && suit == other.suit
}

object Card {
  private var _all: ListBuffer[Card] = new ListBuffer
  private var _masks: ListBuffer[Int] = new ListBuffer

  val All = for { kind <- Kind.All; suit <- Suit.All } yield(new Card(kind, suit))
  val CardsNum = All.size
  val Masks: List[Int] = for { kind <- Kind.All; suit <- Suit.All } yield(kind.toInt << (1 << 4 * suit.toInt))
  
  def apply(value: Any): Card = value match {
    case i: Int => parseInt(i)
    case s: String => parseString(s)
    case _ => throw new Error("can't build card: %s".format(value))
  }
  
  implicit def parseInt(i: Int): Card = {
    if (i <= 0 || i > CardsNum)
      throw new Error("invalid card index: %s".format(i))
    new Card(i >> 2, i % 4)
  }
  
  implicit def parseString(s: String): Card = {
    if (s.size != 2)
      throw new Error("can't parse card: %s".format(s))
    val List(kind, suit) = s.toList
    new Card(kind, suit)
  }
}

object Cards {
  case class Binary(v: List[Int])
  
  def apply(value: Any): List[Card] = value match {
    case s: String => parseString(s)
    case Binary(b) => parseBinary(b)
  }
  
  def parseBinary(b: List[Int]): List[Card] = for { i <- b } yield(Card(i))
  
  def parseString(s: String): List[Card] = {
    val regex = """(?i)([akqjt2-9]{1})([schd]{1})""".r
    val matching = for {
      regex(kind, suit) <- regex findAllIn s
    } yield new Card(kind(0), suit(0))
    matching.toList
  }
  
  val OrderingByHead = Ordering.by[List[Card], Card](_.head)
  val OrderingByMax = Ordering.by[List[Card], Card](_.max)
}
