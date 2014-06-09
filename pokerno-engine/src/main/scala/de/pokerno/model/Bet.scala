package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import java.util.Locale
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonValue, JsonAutoDetect, JsonProperty, JsonPropertyOrder, JsonCreator}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize}

import beans._

@JsonCreator
class BetLike(
    @JsonProperty("call")   val call: Option[Decimal] = None,
    @JsonProperty("raise")  val raise: Option[Decimal] = None,
    @JsonProperty("fold")   val fold: Option[Boolean] = None,
    @JsonProperty("check")  val check: Option[Boolean] = None
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
  
  override def equals(_other: Any): Boolean = {
    
    if (isInstanceOf[Bet.Active]) {
      val active = asInstanceOf[Bet.Active]
      _other match {
        case other: Bet.Active =>
          return super.equals(other) && active.amount == other.amount
        case _ =>
      }
    }

    super.equals(_other)
  }
  
  def isActive: Boolean = false
  def isPassive: Boolean = false
  def isForced: Boolean = false
  def isRaise: Boolean = false
  def isCheck: Boolean = false
  def isCall: Boolean = false
  def isFold: Boolean = false
}

object Bet {
  trait Active extends Bet {
    @JsonIgnore def amount: Decimal
    override def isActive = true
  }
  
  case class Raise(@JsonIgnore amount: Decimal) extends Active {
    def name = "raise"
    @JsonProperty("raise") def raise = amount
  }
  case class Call(@JsonIgnore amount: Decimal) extends Active {
    def name = "call"
    @JsonProperty("call") def call = amount
  }
  
  trait Passive extends Bet {
    override def isPassive = true
  }
  case object Check extends Passive {
    override def isCheck = true
    def name = "check"
    @JsonProperty("check") final val check = true
  }
  case object Fold extends Passive {
    override def isFold = true
    def name = "fold"
    @JsonProperty("fold") final val fold = true
  }
  
  @JsonPropertyOrder(Array("type", "call"))
  abstract class Forced extends Active {
    override def isForced = true
    @JsonProperty("call") def call = amount
    @JsonProperty("type") def betType: ForcedType
  }
  
  trait ForcedType {
    @JsonValue def name: String
    
    def force(amt: Decimal): Bet
  }

  object Ante extends ForcedType {
    def name = "ante"
    def force(amt: Decimal) = Bet.ante(amt)
  }
  case class Ante(amount: Decimal) extends Forced {
    def name = Ante.name
    def betType = Ante 
  }
  object BringIn extends ForcedType {
    def name = "bring-in"
    def force(amt: Decimal) = Bet.bringIn(amt)
  }
  case class BringIn(amount: Decimal) extends Forced {
    def name = BringIn.name
    def betType = BringIn
  }
  object SmallBlind extends ForcedType {
    def name = "small-blind"
    def force(amt: Decimal) = Bet.sb(amt)
  }
  case class SmallBlind(amount: Decimal) extends Forced {
    def name = SmallBlind.name
    def betType = SmallBlind
  }
  object BigBlind extends ForcedType {
    def name = "big-blind"
    def force(amt: Decimal) = Bet.bb(amt)
  }
  case class BigBlind(amount: Decimal) extends Forced {
    def name = BigBlind.name
    def betType = BigBlind
  }
  object GuestBlind extends ForcedType {
    def name = "guest-blind"
    def force(amt: Decimal) = Bet.gb(amt)
  }
  case class GuestBlind(amount: Decimal) extends Forced {
    def name = GuestBlind.name
    def betType = GuestBlind
  }
  object Straddle extends ForcedType {
    def name = "straddle"
    def force(amt: Decimal) = Bet.straddle(amt)
  }
  case class Straddle(amount: Decimal) extends Forced {
    def name = Straddle.name
    def betType = Straddle
  }
  
  case object AllIn extends Bet {
    def name = "all-in"
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
