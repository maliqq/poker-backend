package de.pokerno.poker

object Kind {
  object Value extends Enumeration {
    class Kind(i: Int, name: String) extends Val(i, name) {
      def toInt: Int = id
      def short: Char = _short(id)
      override def toString(): String = short.toString()
    }

    private def Kind(name: String) = new Kind(nextId, name)

    val Deuce   = Kind("deuce")
    val Three   = Kind("three")
    val Four    = Kind("four")
    val Five    = Kind("five")
    val Six     = Kind("six")
    val Seven   = Kind("seven")
    val Eight   = Kind("eight")
    val Nine    = Kind("nine")
    val Ten     = Kind("ten")
    val Jack    = Kind("jack")
    val Queen   = Kind("queen")
    val King    = Kind("king")
    val Ace     = Kind("ace")
  }
  import Value._

  private final val _short = "23456789TJQKA".toList

  final val NumericValues = List range (0, 12)

  implicit def char2Kind(c: Char): Value.Kind = Kinds(_short.indexOf(c))
  implicit def byte2Kind(b: Byte): Value.Kind = Kinds(b)
  implicit def int2Kind(i: Int): Value.Kind = Kinds(i)
}
