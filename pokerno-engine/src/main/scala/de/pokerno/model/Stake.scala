package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

import beans._
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonInclude, JsonProperty, JsonCreator}

object Stake {
  def apply(bigBlind: Decimal,
      smallBlind: Option[Decimal] = None,
      ante: Either[Decimal, Boolean] = Right(false),
      bringIn: Either[Decimal, Boolean] = Right(false)) = new Stake(bigBlind, smallBlind, ante, bringIn)
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Stake(
    @JsonProperty val bigBlind: Decimal,
    _smallBlind: Option[Decimal] = None,
    _ante: Either[Decimal, Boolean] = Right(false),
    _bringIn: Either[Decimal, Boolean] = Right(false)) {
  
  @JsonCreator
  def this(
      @JsonProperty("bigBlind") _bb: Decimal,
      @JsonProperty("smallBlind") _sb: Decimal,
      @JsonProperty("ante") _ante: Decimal,
      @JsonProperty("bringIn") _bringIn: Decimal
  ) = this(_bb, Option[Decimal](_sb), if (_ante == null) Right(false) else Left(_ante), if (_bringIn == null) Right(false) else Left(_bringIn))

  def amount(t: Bet.ForcedType): Decimal = t match {
    case Bet.BringIn    ⇒ bringIn.get
    case Bet.Ante       ⇒ ante.getOrElse(rate(Bet.Ante))
    case Bet.SmallBlind ⇒ smallBlind
    case Bet.BigBlind   ⇒ bigBlind
    case _              ⇒ throw new Error("no amount for %s" format t)
  }

  @JsonProperty val smallBlind: Decimal = _smallBlind getOrElse rate(Bet.SmallBlind)

  @JsonProperty val ante: Option[Decimal] = _ante match {
    case Left(amount) ⇒
      if (amount > .0) Some(amount)
      else None
    case Right(withAnte) ⇒
      if (withAnte) Some(rate(Bet.Ante))
      else None
  }

  @JsonProperty val bringIn: Option[Decimal] = _bringIn match {
    case Left(amount) ⇒
      if (amount > .0) Some(amount)
      else None
    case Right(withBringIn) ⇒
      if (withBringIn) Some(rate(Bet.BringIn))
      else None
  }

  private def rate(v: Bet.ForcedType): Decimal = Rates(v) * bigBlind
}
