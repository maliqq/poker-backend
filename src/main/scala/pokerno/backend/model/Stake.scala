package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}

object Rates {
  final val Default: Map[Bet.Value, Decimal] = Map(
      Bet.BringIn -> 0.125,
      Bet.Ante -> 0.25,
      Bet.SmallBlind -> 0.5,
      Bet.BigBlind -> 1.0,
      Bet.DoubleBet -> 2.0
  )
}

class Stake(
    val bigBlind: Decimal,
    SmallBlind: Option[Decimal] = None,
    Ante: Either[Decimal, Boolean] = Right(false),
    BringIn: Either[Decimal, Boolean]= Right(false)) {
 
  def amount(t: Bet.Value): Decimal = t match {
      case Bet.BringIn => bringIn.get
      case Bet.Ante => ante.get
      case Bet.SmallBlind => smallBlind
      case Bet.BigBlind => bigBlind
      case Bet.DoubleBet => doubleBet
    }
  
  private def _rate(t: Bet.Value): Decimal = Rates.Default(t) * bigBlind
  
  val smallBlind: Decimal = SmallBlind.getOrElse(_rate(Bet.SmallBlind))
  val doubleBet: Decimal = _rate(Bet.DoubleBet)
  
  val ante: Option[Decimal] = Ante match {
    case Left(amount) =>
      if (amount > .0) Some(amount)
      else None
    case Right(withAnte) =>
      if (withAnte) Some(_rate(Bet.Ante))
      else None
  }
  
  val bringIn: Option[Decimal] = BringIn match {
    case Left(amount) =>
      if (amount > .0) Some(amount)
      else None
    case Right(withBringIn) =>
      if (withBringIn) Some(_rate(Bet.BringIn))
      else None
  }
}
