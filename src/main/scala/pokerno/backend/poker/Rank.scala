package pokerno.backend.poker

object Rank {
  abstract class Type
  
  final val high: List[High] = List(StraightFlush, FourKind, FullHouse, Flush, Straight, ThreeKind, TwoPair, OnePair, HighCard)

  sealed class High extends Type with Ordered[High] {
    final val ranks: List[High] = high
    def compare(other: High): Int = ranks.indexOf(this) compare ranks.indexOf(other)
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

  final val badugi: List[Badugi] = List(BadugiFour, BadugiThree, BadugiTwo, BadugiOne)
  
  sealed class Badugi extends Type with Ordered[Badugi] {
    final val ranks: List[Badugi] = badugi
    def compare(other: Badugi): Int = ranks.indexOf(this) compare ranks.indexOf(other)
  }

  case object BadugiOne extends Badugi
  case object BadugiTwo extends Badugi
  case object BadugiThree extends Badugi
  case object BadugiFour extends Badugi

  sealed class Low extends Type with Ordered[Low] {
    final val ranks = List()
    def compare(other: Low): Int = ranks.indexOf(this) compare ranks.indexOf(other)
  }

  case object CompleteLow extends Low
  case object IncompleteLow extends Low
}
