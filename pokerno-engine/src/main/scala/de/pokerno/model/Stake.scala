package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

case class Stake(
    bigBlind: Decimal,
    SmallBlind: Option[Decimal] = None,
    Ante: Either[Decimal, Boolean] = Right(false),
    BringIn: Either[Decimal, Boolean] = Right(false)) {

  def amount(t: BetType.Value): Decimal = t match {
    case BetType.BringIn    ⇒ bringIn.get
    case BetType.Ante       ⇒ ante.getOrElse(rate(BetType.Ante))
    case BetType.SmallBlind ⇒ smallBlind
    case BetType.BigBlind   ⇒ bigBlind
    case _              ⇒ throw new Error("no amount for %s" format t)
  }

  val smallBlind: Decimal = SmallBlind getOrElse rate(BetType.SmallBlind)

  val ante: Option[Decimal] = Ante match {
    case Left(amount) ⇒
      if (amount > .0) Some(amount)
      else None
    case Right(withAnte) ⇒
      if (withAnte) Some(rate(BetType.Ante))
      else None
  }

  val bringIn: Option[Decimal] = BringIn match {
    case Left(amount) ⇒
      if (amount > .0) Some(amount)
      else None
    case Right(withBringIn) ⇒
      if (withBringIn) Some(rate(BetType.BringIn))
      else None
  }

  private def rate(v: BetType.Value) = Rates(v) * bigBlind
}
