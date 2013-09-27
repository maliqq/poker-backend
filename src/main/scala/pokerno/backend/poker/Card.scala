package pokerno.backend.poker

import scala.collection.mutable.ListBuffer

class Card(val kind: Kind.Value, val suit: Suit.Value) {
  def colored = "%s%s %s%s".format(suit.color, kind.toString, suit.toString, Console.RESET)
  override def toString = kind.toString + suit.toString
}

object Card {
  private var _all: ListBuffer[Card] = new ListBuffer
  Kind.All foreach { kind =>
    Suit.All foreach { suit =>
      _all += new Card(kind, suit)
    }
  }
  val All = _all
}

object Suit {
  trait Value {
    def short: Char
    def unicode: String
    def color: String
    override def toString: String = short.toString
  }
  
  case object Spade extends Value {
    def short = 's'
    def unicode = "♠"
    def color = Console.YELLOW
  }
  case object Heart extends Value {
    def short = 'h'
    def unicode = "♥"
    def color = Console.RED
  }
  case object Diamond extends Value {
    def short = 'd'
    def unicode = "♦"
    def color = Console.CYAN
  }
  case object Club extends Value {
    def short = 'c'
    def unicode = "♣"
    def color = Console.GREEN
  }
  
  val All: List[Value] = List(Spade, Heart, Diamond, Club)
}

object Kind {
  trait Value {
    def short: Char
    override def toString: String = short.toString
  }
  
  case object Deuce extends Value {
    def short = '2'
  }
  case object Three extends Value {
    def short = '3'
  }
  case object Four extends Value {
    def short = '4'
  }
  case object Five extends Value {
    def short = '5'
  }
  case object Six extends Value {
    def short = '6'
  }
  case object Seven extends Value {
    def short = '7'
  }
  case object Eight extends Value {
    def short = '8'
  }
  case object Nine extends Value {
    def short = '9'
  }
  case object Ten extends Value {
    def short = 'T'
  }
  case object Jack extends Value {
    def short = 'J'
  }
  case object Queen extends Value {
    def short = 'Q'
  }
  case object King extends Value {
    def short = 'K'
  }
  case object Ace extends Value {
    def short = 'A'
  }
  
  val All: List[Value] = List(Deuce, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace)
}
