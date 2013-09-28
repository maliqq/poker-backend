package pokerno.backend.model

import scala.math.{BigDecimal => Decimal}

class Bet(val betType: Bet.Value, val amount: Decimal = .0) {
  override def toString = {
    if (amount > .0)
      "%s %.2f".format(betType.toString, amount)
    else
      betType.toString
  }
}

object Bet {
  type Range = Tuple2[Decimal, Decimal]
  
  case class Requirement(private var _call: Option[Decimal], private var _raise: Option[Range] = None) {
    def call = _call
    def raise = _raise
    def reset {
      _call = None
      _raise = None
    }
    def disableRaise {
      _raise = None
    }
    def raise_=(value: Range) {
      _raise = Some((_call.get + value._1, _call.get + value._2))
    }
    
    def available_=(value: Decimal) {
      // FIXME .get
      val (min, max) = _raise.get
      
      if (value < max) {
        if (value < _call.get)
          _raise = None
        else if (value < min)
          _raise = Some((value, value))
        else
          _raise = Some((min, value))
      }
    }
  }
  
  trait Value
  
  abstract class ForcedBet extends Value
  abstract class PassiveBet extends Value
  abstract class ActiveBet extends Value
  object DoubleBet extends Value
  abstract class CardAction extends Value
  
  case object SmallBlind extends ForcedBet
  case object BigBlind extends ForcedBet
  case object Ante extends ForcedBet
  case object BringIn extends ForcedBet
  case object GuestBlind extends ForcedBet
  case object Straddle extends ForcedBet
  
  case object Fold extends ActiveBet
  case object Call extends ActiveBet
  
  case object Raise extends PassiveBet
  case object Check extends PassiveBet
  
  case object Discard extends CardAction
  case object StandPat extends CardAction
  case object Show extends CardAction
  case object Muck extends CardAction
  
  def check = new Bet(Check)
  def fold = new Bet(Fold)
  def call(amount: Decimal) = new Bet(Call, amount)
  def raise(amount: Decimal) = new Bet(Raise, amount)
  
  def force(t: Bet.Value, stake: Stake):Bet = new Bet(t, stake.amount(t))
  
  trait Validator {
  b : Bet =>
    def validate(seat: Seat, r: Requirement) = b.betType match {
      case Fold => Unit
      
      case Check =>
        if (r.call != seat.bet)
          throw new Error("Can't check: need to call=%.2f".format(r.call))
      
      case Call | Raise =>
        if (amount > seat.amount.get)
          throw new Error("Can't bet: got amount=%.2f, stack=%.2f".format(amount, seat.amount.get))

        if (betType == Call)
          validateRange(amount, r.call.get, r.call.get, amount == seat.amount)
        if (betType == Raise)
          validateRange(amount, r.raise.get._1, r.raise.get._2, amount == seat.amount)
    }

    def validateRange(amount: Decimal, min: Decimal, max: Decimal, allIn: Boolean = false) {
      if (max == 0.)
        throw new Error("Nothing to bet: got amount=%.2f".format(amount))
      
      if (amount > max)
        throw new Error("Bet invalid: got amount=%.2f, required max=%.2f".format(amount, max))
      
      if (amount < min && !allIn)
        throw new Error("Bet invalid: got amount=%.2f, required min=%.2f".format(amount, min))
      
    }
  }
}
