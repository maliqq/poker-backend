package pokerno.backend.poker

object Rank {
  trait Type {
    def compare(other: Type): Int
  }

  object High extends Enumeration {
    class High(i: Int, name: String) extends Val(i, name) with Type

    private def High(name: String) = new High(nextId, name)

    val HighCard =        High("high-card")
    val OnePair =         High("one-pair")
    val TwoPair =         High("two-pair")
    val ThreeKind =       High("three-kind")
    val Straight =        High("straight")
    val Flush =           High("flush")
    val FullHouse =       High("full-house")
    val FourKind =        High("four-kind")
    val StraightFlush =   High("straight-flush")
  }

  import High._

  object Badugi extends Enumeration {
    class Badugi(i: Int, name: String) extends Val(i, name) with Type

    private def Badugi(name: String): Badugi = new Badugi(nextId, name)

    val BadugiOne =       Badugi("badugi-one")
    val BadugiTwo =       Badugi("badugi-two")
    val BadugiThree =     Badugi("badugi-three")
    val BadugiFour =      Badugi("badugi-four")
  }
  import Badugi._

  object Low extends Enumeration {
    class Low(i: Int, name: String) extends Val(i, name) with Type

    private def Low(name: String): Low = new Low(nextId, name)

    val Complete =        Low("complete-low")
    val Incomplete =      Low("incomplete-low")
  }
  import Low._
}
