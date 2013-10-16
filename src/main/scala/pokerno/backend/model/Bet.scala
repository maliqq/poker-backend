package pokerno.backend.model

import scala.math.{ BigDecimal ⇒ Decimal }
import java.util.Locale

class Bet(val betType: Bet.Value, val amount: Decimal = .0) {
  override def toString =
    if (amount > .0) "%s %.2f" formatLocal (Locale.US, betType toString, amount)
    else betType toString

  def isValid(available: Decimal, alreadyPut: Decimal, needToCall: Decimal, range: Range): Boolean = betType match {
    case Bet.Fold ⇒
      true

    case Bet.Check ⇒
      needToCall == alreadyPut

    case Bet.Call | Bet.Raise ⇒
      (amount <= available &&
        ((betType == Bet.Call && Range(needToCall, needToCall).isValid(amount, available)) ||
          (betType == Bet.Raise && range.isValid(amount, available)))
      )
    
    case _: Bet.ForcedBet =>
      amount == needToCall || (amount < needToCall && amount == available)

    case _ ⇒ false
  }
}

object Bet {
  trait Value

  trait Rateable {
    v: Value ⇒
    def rateWith(amount: Decimal): Decimal = Rates.Default(this) * amount
  }

  object DoubleBet extends Value with Rateable

  abstract class ForcedBet extends Value
  case object SmallBlind extends ForcedBet with Rateable
  case object BigBlind extends ForcedBet with Rateable
  case object Ante extends ForcedBet with Rateable
  case object BringIn extends ForcedBet with Rateable
  case object GuestBlind extends ForcedBet
  case object Straddle extends ForcedBet

  abstract class PassiveBet extends Value
  case object Fold extends PassiveBet
  case object Call extends PassiveBet

  abstract class ActiveBet extends Value
  case object Raise extends ActiveBet
  case object Check extends ActiveBet

  abstract class CardAction extends Value
  case object Discard extends CardAction
  case object StandPat extends CardAction
  case object Show extends CardAction
  case object Muck extends CardAction

  def check = new Bet(Check)
  def fold = new Bet(Fold)
  def call(amount: Decimal) = new Bet(Call, amount)
  def raise(amount: Decimal) = new Bet(Raise, amount)

  case class CantCheck(call: Decimal)
    extends Error("Can't check: need to call=%.2f" format (call))

  case class CantBet(amount: Decimal, stack: Decimal)
    extends Error("Can't bet: got amount=%.2f, stack=%.2f" format (amount, stack))
}
