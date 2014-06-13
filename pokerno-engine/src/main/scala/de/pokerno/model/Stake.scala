package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import beans._
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonInclude, JsonProperty, JsonCreator}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

object Stake {
  private def BBs(bigBlind: Decimal, v: BetType.Forced): Decimal = Rates(v) * bigBlind

  def apply(bigBlind: Decimal,
      smallBlind: Option[Decimal] = None,
      ante: Either[Decimal, Boolean] = Right(false),
      bringIn: Either[Decimal, Boolean] = Right(false)): Stake = {
      
    def bbs(v: BetType.Forced): Decimal = {
      Rates(v) * bigBlind
    }
    
    val _ante = ante match {
        case Left(amount) ⇒
          if (amount > .0) Some(amount)
          else None
        case Right(withAnte) ⇒
          if (withAnte) Some(bbs(BetType.Ante))
          else None
      }
    
    val _bringIn = bringIn match {
        case Left(amount) ⇒
          if (amount > .0) Some(amount)
          else None
        case Right(withBringIn) ⇒
          if (withBringIn) Some(bbs(BetType.BringIn))
          else None
      }
    
    Stake(
      bigBlind,
      smallBlind getOrElse bbs(BetType.SmallBlind),
      _ante,
      _bringIn
    )
  }
}

class StakeBuilder {
  @JsonProperty var bigBlind: Decimal = null
  @JsonProperty var smallBlind: Option[Decimal] = None
  @JsonProperty var ante: Option[Decimal] = None
  @JsonProperty var bringIn: Option[Decimal] = None
  
  def build(): Stake = {
    assert(bigBlind != null) // FIXME
    Stake(
      bigBlind,
      smallBlind,
      if (ante.isDefined) Left(ante.get)
      else Right(false),
      if (bringIn.isDefined) Left(bringIn.get)
      else Right(false)
    )
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = classOf[StakeBuilder])
case class Stake(
    @JsonProperty bigBlind: Decimal,
    @JsonProperty smallBlind: Decimal,
    @JsonProperty ante: Option[Decimal],
    @JsonProperty bringIn: Option[Decimal]) {

  def amount(t: BetType.Forced): Decimal = t match {
    case BetType.BringIn    ⇒ bringIn.get
    case BetType.Ante
      if ante.isDefined ⇒ ante.get
    case BetType.SmallBlind ⇒ smallBlind
    case BetType.BigBlind   ⇒ bigBlind
    case _              ⇒ throw new Error("no amount for %s" format t)
  }

}
