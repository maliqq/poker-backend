package de.pokerno.model

import beans._
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonInclude, JsonProperty, JsonCreator}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

object Stake {
  private def BBs(bigBlind: Decimal, v: BetType.Forced): Decimal = Rates(v) * bigBlind

  def apply(bigBlind: Decimal,
      smallBlind: Option[Decimal] = None,
      buyIn: Tuple2[Int, Int] = BuyIn.Default,
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
      BuyIn.Default,
      _ante,
      _bringIn
    )
  }
}

class StakeBuilder {
  @JsonProperty("big_blind") var bigBlind: Decimal = null
  @JsonProperty("small_blind") var smallBlind: Option[Decimal] = None
  @JsonProperty var ante: Option[Decimal] = None
  @JsonProperty("bring_in") var bringIn: Option[Decimal] = None
  @JsonProperty("buy_in") var buyIn: Option[Tuple2[Int, Int]] = None
  
  def build(): Stake = {
    assert(bigBlind != null) // FIXME
    Stake(
      bigBlind,
      smallBlind,
      buyIn getOrElse(BuyIn.Default),
      ante.map(Left(_)) getOrElse(Right(false)),
      bringIn.map(Left(_)) getOrElse(Right(false))
    )
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = classOf[StakeBuilder])
case class Stake(
    @JsonProperty("big_blind") bigBlind: Decimal,
    @JsonProperty("small_blind") smallBlind: Decimal,
    @JsonProperty("buy_in") buyIn: Tuple2[Int, Int],
    @JsonProperty ante: Option[Decimal],
    @JsonProperty("bring_in") bringIn: Option[Decimal]) {

  def buyInAmount: Tuple2[Decimal, Decimal] = (bigBlind * buyIn._1, bigBlind * buyIn._2)
  
  def amount(t: BetType.Forced): Decimal = t match {
    case BetType.BringIn
      if bringIn.isDefined  ⇒ bringIn.get
    case BetType.Ante
      if ante.isDefined     ⇒ ante.get
    case BetType.SmallBlind ⇒ smallBlind
    case BetType.BigBlind   ⇒ bigBlind
    case _              ⇒ throw new Error("no amount for %s" format t)
  }

}
