package de.pokerno.poker

object Suit {
  abstract class Value {
    def short: Char
    def unicode: String
    def toInt: Int
    def color: String
    override def toString: String = short.toString()
  }
  case object Spade extends Value {
    def toInt = 0
    def short = 's'
    def unicode = "♠"
    def color = Console.YELLOW
  }
  case object Heart extends Value {
    def toInt = 1
    def short = 'h'
    def unicode = "♥"
    def color = Console.RED
  }
  case object Diamond extends Value {
    def toInt = 2
    def short = 'd'
    def unicode = "♦"
    def color = Console.CYAN
  }
  case object Club extends Value {
    def toInt = 3
    def short = 'c'
    def unicode = "♣"
    def color = Console.GREEN
  }

  implicit def valueToInt(suit: Value): Int = suit.toInt
  implicit def charToSuit(char: Char): Value = char match {
    case 's' ⇒ Spade
    case 'h' ⇒ Heart
    case 'd' ⇒ Diamond
    case 'c' ⇒ Club
  }
  implicit def intToSuit(i: Int): Value = Suits(i)

  final val Seq = List range (0, 4)
}
