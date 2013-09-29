package pokerno.backend.poker

object Rank {
  abstract class Type extends Ordered[Type] {
    def ranks: List[Type]
    def compare(other: Type) = ranks.indexOf(this) compare ranks.indexOf(other)
  }
  
  sealed class High extends Type {
    val ranks: List[High] = List(StraightFlush, FourKind, FullHouse, Flush, Straight, ThreeKind, TwoPair, OnePair, HighCard)
  }
  
  case object StraightFlush extends High
  case object FourKind extends High
  case object FullHouse extends High
  case object Flush extends High
  case object Straight extends High
  case object ThreeKind extends High
  case object TwoPair extends High
  case object OnePair extends High
  case object HighCard extends High
  
  sealed class Badugi extends Type {
    val ranks: List[Badugi] = List(BadugiOne, BadugiTwo, BadugiThree, BadugiFour)
  }
  
  case object BadugiOne extends Badugi
  case object BadugiTwo extends Badugi
  case object BadugiThree extends Badugi
  case object BadugiFour extends Badugi
  
  sealed class Low extends Type {
    val ranks = List()
  }
  
  case object CompleteLow extends Low
  case object IncompleteLow extends Low
}
