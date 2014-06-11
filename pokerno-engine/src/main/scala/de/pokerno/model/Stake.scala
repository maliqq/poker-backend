package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }

import beans._
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonInclude, JsonProperty, JsonCreator}

object Stake {
  private def BBs(bigBlind: Decimal, v: Bet.ForcedType): Decimal = Rates(v) * bigBlind

  def apply(bigBlind: Decimal,
      smallBlind: Option[Decimal] = None,
      ante: Either[Decimal, Boolean] = Right(false),
      bringIn: Either[Decimal, Boolean] = Right(false)): Stake = {
      
    def bbs(v: Bet.ForcedType): Decimal = {
      Rates(v) * bigBlind
    }
    
    val _ante = ante match {
        case Left(amount) ⇒
          if (amount > .0) Some(amount)
          else None
        case Right(withAnte) ⇒
          if (withAnte) Some(bbs(Bet.Ante))
          else None
      }
    
    val _bringIn = bringIn match {
        case Left(amount) ⇒
          if (amount > .0) Some(amount)
          else None
        case Right(withBringIn) ⇒
          if (withBringIn) Some(bbs(Bet.BringIn))
          else None
      }
    
    Stake(
      bigBlind,
      smallBlind getOrElse bbs(Bet.SmallBlind),
      _ante,
      _bringIn
    )
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonCreator
case class Stake(
    @JsonProperty bigBlind: Decimal,
    @JsonProperty smallBlind: Decimal,
    @JsonProperty ante: Option[Decimal],
    @JsonProperty bringIn: Option[Decimal]) {

  def amount(t: Bet.ForcedType): Decimal = t match {
    case Bet.BringIn    ⇒ bringIn.get
    case Bet.Ante
      if ante.isDefined ⇒ ante.get
    case Bet.SmallBlind ⇒ smallBlind
    case Bet.BigBlind   ⇒ bigBlind
    case _              ⇒ throw new Error("no amount for %s" format t)
  }

}
