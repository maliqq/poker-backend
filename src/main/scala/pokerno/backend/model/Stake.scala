package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}
import scala.reflect.runtime.universe._

object Rates {
  val Default: Map[Type, Decimal] = Map(
      typeOf[Bet.BringIn] -> 0.125,
      typeOf[Bet.Ante] -> 0.25,
      typeOf[Bet.SmallBlind] -> 0.5,
      typeOf[Bet.BigBlind] -> 1.0,
      typeOf[Bet.DoubleBet] -> 2.0
  )
}

class Stake(
    val bigBlind: Decimal,
    SmallBlind: Option[Decimal] = None,
    Ante: Either[Decimal, Boolean] = Right(false),
    BringIn: Either[Decimal, Boolean]= Right(false)) {
 
  def amount[T : Manifest]: Decimal = manifest[T] match {
      case Bet.bringIn => bringIn.get
      case Bet.ante => ante.get
      case Bet.smallBlind => smallBlind
      case Bet.bigBlind => bigBlind
      case Bet.doubleBet => doubleBet
    }
  
  private def _rated(t: Type): Decimal = Rates.Default(t) * bigBlind
  
  val smallBlind: Decimal = SmallBlind.getOrElse(_rated(typeOf[Bet.SmallBlind]))
  val doubleBet: Decimal = _rated(typeOf[Bet.DoubleBet])
  
  val ante: Option[Decimal] = Ante match {
    case Left(amount) =>
      if (amount > .0)
        Some(amount)
      else
        None
    case Right(withAnte) =>
      if (withAnte)
        Some(_rated(typeOf[Bet.Ante]))
      else
        None
  }
  
  val bringIn: Option[Decimal] = BringIn match {
    case Left(amount) =>
      if (amount > .0)
        Some(amount)
      else
        None
    case Right(withBringIn) =>
      if (withBringIn)
        Some(_rated(typeOf[Bet.BringIn]))
      else
        None
  }
}
