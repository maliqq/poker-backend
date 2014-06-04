package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import beans._
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonInclude, JsonProperty}

@JsonInclude(JsonInclude.Include.NON_NULL)
case class Stake(
    @JsonProperty bigBlind: Decimal,
    @JsonIgnore SmallBlind: Option[Decimal] = None,
    @JsonIgnore Ante: Either[Decimal, Boolean] = Right(false),
    @JsonIgnore BringIn: Either[Decimal, Boolean] = Right(false)) {

  def amount(t: Bet.ForcedType): Decimal = t match {
    case Bet.BringIn    ⇒ bringIn.get
    case Bet.Ante       ⇒ ante.getOrElse(rate(Bet.Ante))
    case Bet.SmallBlind ⇒ smallBlind
    case Bet.BigBlind   ⇒ bigBlind
    case _              ⇒ throw new Error("no amount for %s" format t)
  }

  @JsonProperty val smallBlind: Decimal = SmallBlind getOrElse rate(Bet.SmallBlind)

  @JsonProperty val ante: Option[Decimal] = Ante match {
    case Left(amount) ⇒
      if (amount > .0) Some(amount)
      else None
    case Right(withAnte) ⇒
      if (withAnte) Some(rate(Bet.Ante))
      else None
  }

  @JsonProperty val bringIn: Option[Decimal] = BringIn match {
    case Left(amount) ⇒
      if (amount > .0) Some(amount)
      else None
    case Right(withBringIn) ⇒
      if (withBringIn) Some(rate(Bet.BringIn))
      else None
  }

  private def rate(v: Bet.ForcedType): Decimal = Rates(v) * bigBlind
}
