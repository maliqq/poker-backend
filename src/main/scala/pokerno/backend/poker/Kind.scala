package pokerno.backend.poker

object Kind {
  private val _short = "23456789TJQK".toList
  private val _full: List[String] = List("deuce", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "jack", "queen", "king", "ace")
  
  abstract class Value(val toInt: Int = All.indexOf(this)) {
    def short: Char = _short(toInt)
    def full: String = _full(toInt)
    override def toString: String = short.toString
  }
  
  implicit def int2Value(i: Int): Value = All(i)
  
  case object Deuce extends Value
  case object Three extends Value
  case object Four extends Value
  case object Five extends Value
  case object Six extends Value
  case object Seven extends Value
  case object Eight extends Value
  case object Nine extends Value
  case object Ten extends Value
  case object Jack extends Value
  case object Queen extends Value
  case object King extends Value
  case object Ace extends Value
  
  val All: List[Value] = List(Deuce, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace)
}
