package pokerno.backend.model

import scala.math.{ BigDecimal ⇒ Decimal }

object Rates {
  final val Default: Map[Bet.Value, Decimal] = Map(
    Bet.Ante -> 0.125,
    Bet.BringIn -> 0.25,
    Bet.SmallBlind -> 0.5,
    Bet.BigBlind -> 1.0,
    Bet.DoubleBet -> 2.0)
}

class Stake(
    val bigBlind: Decimal,
    SmallBlind: Option[Decimal] = None,
    Ante: Either[Decimal, Boolean] = Right(false),
    BringIn: Either[Decimal, Boolean] = Right(false)) {

  def amount(t: Bet.Value): Decimal = t match {
    case Bet.BringIn    ⇒ bringIn get
    case Bet.Ante       ⇒ ante get
    case Bet.SmallBlind ⇒ smallBlind
    case Bet.BigBlind   ⇒ bigBlind
    case Bet.DoubleBet  ⇒ doubleBet
    case _ => throw new Error("no amount for %s" format(t))
  }

  val smallBlind: Decimal = SmallBlind getOrElse (Bet.SmallBlind rateWith (bigBlind))
  val doubleBet: Decimal = Bet.DoubleBet rateWith (bigBlind)

  val ante: Option[Decimal] = Ante match {
    case Left(amount) ⇒
      if (amount > .0) Some(amount)
      else None
    case Right(withAnte) ⇒
      if (withAnte) Some(Bet.Ante rateWith (bigBlind))
      else None
  }

  val bringIn: Option[Decimal] = BringIn match {
    case Left(amount) ⇒
      if (amount > .0) Some(amount)
      else None
    case Right(withBringIn) ⇒
      if (withBringIn) Some(Bet.BringIn rateWith (bigBlind))
      else None
  }
}
