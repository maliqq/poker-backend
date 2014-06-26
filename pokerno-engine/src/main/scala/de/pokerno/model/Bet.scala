package de.pokerno.model

import java.util.Locale
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonValue, JsonAutoDetect, JsonProperty, JsonPropertyOrder, JsonCreator}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize}

import beans._

@JsonCreator
class BetLike(
    @JsonProperty("call")   val call: Option[Decimal] = None,
    @JsonProperty("raise")  val raise: Option[Decimal] = None,
    @JsonProperty("fold")   val fold: Option[Boolean] = None,
    @JsonProperty("check")  val check: Option[Boolean] = None,
    @JsonProperty("allin")  val allin: Option[Boolean] = None // TODO
) {
  def bet = call.map { amt =>
    Bet.call(amt)
  } orElse raise.map { amt =>
    Bet.raise(amt)
  } orElse fold.map { fold =>
    Bet.fold
  } orElse check.map { check =>
    Bet.check
  } getOrElse Bet.fold
}

class BetConverter extends com.fasterxml.jackson.databind.util.StdConverter[BetLike, Bet] {
  def convert(b: BetLike): Bet = b.bet
}

@JsonDeserialize(converter = classOf[BetConverter])
@JsonAutoDetect(isGetterVisibility = JsonAutoDetect.Visibility.NONE)
trait Bet {
  def name: String
  
  override def toString: String = this match {
    case active: Bet.Active =>
      "%s %.2f" formatLocal (Locale.US, name, active.amount)
    case _ => name
  }
  
  override def equals(o: Any): Boolean = o match {
    case _other: Bet =>
      if (isActive && _other.isActive) {
        val active = asInstanceOf[Bet.Active]
        val other = _other.asInstanceOf[Bet.Active]
        active.betType == other.betType && active.amount == other.amount
      } else betType == _other.betType
    
    case _ => false
  }
  
  def betType: BetType
  
  def isActive: Boolean = isInstanceOf[Bet.Active]
  def toActive: Bet.Active = asInstanceOf[Bet.Active]
  
  def isForced: Boolean = isInstanceOf[Bet.Forced]
  def toForced: Bet.Forced = asInstanceOf[Bet.Forced]
  
  def isPassive: Boolean = isInstanceOf[Bet.Passive]
  
  def isRaise: Boolean = false
  def isCheck: Boolean = false
  def isCall: Boolean = false
  def isFold: Boolean = false
}

object Bet {
  trait Active extends Bet {
    @JsonIgnore def amount: Decimal
    // @JsonIgnore protected var _isAllIn = false
    // def allIn() = _isAllIn = true
    // def isAllIn: Boolean = _isAllIn
  }
  
  case class Raise(@JsonIgnore amount: Decimal) extends Active {
    def name = "raise"
    def betType = BetType.Raise
    override def isRaise = true
    @JsonProperty("raise") def raise = amount
  }
  case class Call(@JsonIgnore amount: Decimal) extends Active {
    def name = "call"
    def betType = BetType.Call
    override def isCall = true
    @JsonProperty("call") def call = amount
  }
  
  trait Passive extends Bet
  
  case object Check extends Passive {
    def name = "check"
    def betType = BetType.Check
    override def isCheck = true
    @JsonProperty("check") final val check = true
  }
  
  case object Fold extends Passive {
    def name = "fold"
    def betType = BetType.Fold
    override def isFold = true
    @JsonProperty("fold") final val fold = true
  }
  
  @JsonPropertyOrder(Array("type", "call"))
  abstract class Forced extends Active {
    def name = betType.name
    @JsonProperty("call") def call = amount
    @JsonProperty("type") def betType: BetType.Forced
  }
  
  case class SmallBlind(amount: Decimal) extends Forced {
    def betType = BetType.SmallBlind
  }
  case class BigBlind(amount: Decimal) extends Forced {
    def betType = BetType.BigBlind
  }
  case class Ante(amount: Decimal) extends Forced {
    def betType = BetType.Ante 
  }
  case class BringIn(amount: Decimal) extends Forced {
    def betType = BetType.BringIn
  }
  
  case class GuestBlind(amount: Decimal) extends Forced {
    def betType = BetType.GuestBlind
  }
  case class Straddle(amount: Decimal) extends Forced {
    def betType = BetType.Straddle
  }
  
  case object AllIn extends Bet {
    def name = "all-in"
    def betType = BetType.AllIn
  }

  def check()                   = Check
  def fold()                    = Fold
  def call(amount: Decimal)     = Call(amount)
  def raise(amount: Decimal)    = Raise(amount)
  def allIn()                   = AllIn
  
  def sb(amount: Decimal)               = SmallBlind(amount)
  def smallBlind(amount: Decimal)       = SmallBlind(amount)
  def bb(amount: Decimal)               = BigBlind(amount)
  def bigBlind(amount: Decimal)         = BigBlind(amount)
  def ante(amount: Decimal)             = Ante(amount)
  def bringIn(amount: Decimal)          = BringIn(amount)
  def gb(amount: Decimal)               = GuestBlind(amount)
  def guestBlind(amount: Decimal)       = GuestBlind(amount)
  def straddle(amount: Decimal)         = Straddle(amount)

  case class CantCheck(call: Decimal)
    extends Error("Can't check: need to call=%.2f" format call)

  case class CantBet(amount: Decimal, stack: Decimal)
    extends Error("Can't bet: got amount=%.2f, stack=%.2f" format (amount, stack))
}
