package pokerno.backend.poker

object Suit {
  private final val _short = "shdc"
  private final val _unicode = List("♠", "♥", "♦", "♣")
  private final val _colors: List[String] = List(Console.YELLOW, Console.RED, Console.CYAN, Console.GREEN)

  sealed class Value {
    def toInt: Int = All indexOf (this)
    def short: Char = _short(toInt)
    def unicode: String = _unicode(toInt)
    def color: String = _colors(toInt)
    override def toString: String = short toString
  }

  implicit def int2Value(i: Int): Value = All(i)

  case object Spade extends Value
  case object Heart extends Value
  case object Diamond extends Value
  case object Club extends Value

  final val All: List[Value] = List(Spade, Heart, Diamond, Club)
  final val Seq = List range (0, 3)
}
