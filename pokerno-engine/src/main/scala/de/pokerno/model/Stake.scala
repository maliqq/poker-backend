package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

private[model] object Rates {
  final val Default: Map[Bet.Value, Decimal] = Map(
    Bet.Ante -> 0.1,
    Bet.BringIn -> 0.25,
    Bet.SmallBlind -> 0.5,
    Bet.BigBlind -> 1.0)
}

case class Stake(
    bigBlind: Decimal,
    SmallBlind: Option[Decimal] = None,
    Ante: Either[Decimal, Boolean] = Right(false),
    BringIn: Either[Decimal, Boolean] = Right(false)) {

  def amount(t: Bet.Value): Decimal = t match {
    case Bet.BringIn    ⇒ bringIn.get
    case Bet.Ante       ⇒ ante.getOrElse(rate(Bet.Ante))
    case Bet.SmallBlind ⇒ smallBlind
    case Bet.BigBlind   ⇒ bigBlind
    case _              ⇒ throw new Error("no amount for %s" format t)
  }

  val smallBlind: Decimal = SmallBlind getOrElse rate(Bet.SmallBlind)

  val ante: Option[Decimal] = Ante match {
    case Left(amount) ⇒
      if (amount > .0) Some(amount)
      else None
    case Right(withAnte) ⇒
      if (withAnte) Some(rate(Bet.Ante))
      else None
  }

  val bringIn: Option[Decimal] = BringIn match {
    case Left(amount) ⇒
      if (amount > .0) Some(amount)
      else None
    case Right(withBringIn) ⇒
      if (withBringIn) Some(rate(Bet.BringIn))
      else None
  }

  private def rate(v: Bet.Value) = Rates.Default(v) * bigBlind
}
