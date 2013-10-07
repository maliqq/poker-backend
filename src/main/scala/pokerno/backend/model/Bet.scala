package pokerno.backend.model

import scala.math.{ BigDecimal ⇒ Decimal }

class Bet(val betType: Bet.Value, val amount: Decimal = .0) {
  override def toString =
    if (amount > .0) "%s %.2f" format (betType toString, amount)
    else betType.toString
}

object Bet {
  trait Value

  trait Rateable {
    v: Value ⇒
    def rateWith(amount: Decimal): Decimal = Rates.Default(this) * amount
  }

  abstract class ForcedBet extends Value
  abstract class PassiveBet extends Value
  abstract class ActiveBet extends Value
  object DoubleBet extends Value with Rateable
  abstract class CardAction extends Value

  case object SmallBlind extends ForcedBet with Rateable
  case object BigBlind extends ForcedBet with Rateable
  case object Ante extends ForcedBet with Rateable
  case object BringIn extends ForcedBet with Rateable
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

  def force(t: Bet.Value, stake: Stake): Bet = new Bet(t, stake amount (t))

  case class CantCheck(call: Decimal)
    extends Error("Can't check: need to call=%.2f" format (call))

  case class CantBet(amount: Decimal, stack: Decimal)
    extends Error("Can't bet: got amount=%.2f, stack=%.2f" format (amount, stack))

  class Validation(private var _call: Option[Decimal] = None, private var _raise: Option[Range] = None) {
    def call = _call
    def call_=(amount: Decimal) = _call = Some(amount)

    // sets raise range
    def raise = _raise
    def raise_=(r: Range) {
      _raise = Some(r)
    }

    // resets validation
    def reset {
      _call = None
      _raise = None
    }

    // disables raise
    def disableRaise {
      _raise = None
    }

    // adjusts raise range according to available stack
    def adjustRaise(r: Range, value: Decimal) =
      if (value < _call.get)
        _raise = None
      else {
        var min = _call.get + r.min
        var max = _call.get + r.max
        _raise = Some(Range(List(value, min) min, List(value, max) min))
      }

    def validate(bet: Bet, seat: Seat) = bet.betType match {
      case Bet.Fold ⇒
      case Bet.Check ⇒
        if (_call.get != seat.put)
          throw CantCheck(_call.get)

      case Bet.Call | Bet.Raise ⇒
        if (bet.amount > seat.amount.get)
          throw CantBet(bet.amount, seat.amount get)

        val isAllIn = bet.amount == seat.amount
        if (bet.betType == Bet.Call)
          Range(_call.get, _call.get) validate (bet.amount, isAllIn)
        if (bet.betType == Bet.Raise)
          _raise.get validate (bet.amount, isAllIn)
      case _ =>
    }
  }
}
