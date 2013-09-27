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
}
