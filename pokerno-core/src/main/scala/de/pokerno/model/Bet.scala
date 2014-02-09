package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import java.util.Locale

class Bet(val betType: Bet.Value, val amount: Decimal = .0) {
  override def toString =
    if (amount > .0)
      "%s %.2f" formatLocal (Locale.US, betType.toString, amount)
    else betType.toString
//
//  def isValid(left: Decimal, put: Decimal, call: Decimal, raise: Range): Boolean = betType match {
//    case Bet.Fold  ⇒
//      // all-in can't fold
//      left != .0
//      
//    case Bet.Check ⇒
//      left != .0 && call == put
//      
//    case Bet.Call | Bet.Raise ⇒
//      amount <= left &&
//        (if (betType == Bet.Call)
//          amount + put == call
//        else raise.isValid(amount, left))
//
//    case Bet.Call | _: Bet.ForcedBet ⇒
//      amount <= left &&
//        (amount + put == call ||
//        (amount < call && amount == left))
//
//    case _ ⇒ false
//  }

  def isActive: Boolean = amount > .0
  def isForced: Boolean = betType.isInstanceOf[Bet.ForcedBet]
  def isRaise: Boolean = betType == Bet.Raise
  def isCheck: Boolean = betType == Bet.Check
  def isCall: Boolean = betType == Bet.Call
  def isFold: Boolean = betType == Bet.Fold

  override def equals(other: Any): Boolean = {
    other match {
      case b: Bet ⇒ betType == b.betType && amount == b.amount
      case _      ⇒ false
    }
  }
}

object Bet {
  trait Value
  trait Rateable // FIXME - ?

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
  case object Check extends PassiveBet

  abstract class ActiveBet extends Value
  case object Raise extends ActiveBet
  case object AllIn extends ActiveBet
  case object Call extends ActiveBet

  abstract class CardAction extends Value

  def check = new Bet(Check)
  def fold = new Bet(Fold)
  def call(amount: Decimal) = new Bet(Call, amount)
  def raise(amount: Decimal) = new Bet(Raise, amount)
  def allin = new Bet(AllIn)
  def forced(t: ForcedBet, amount: Decimal) = new Bet(t, amount)
  def sb(amount: Decimal) = forced(SmallBlind, amount)
  def bb(amount: Decimal) = forced(BigBlind, amount)
  def ante(amount: Decimal) = forced(Ante, amount)

  case class CantCheck(call: Decimal)
    extends Error("Can't check: need to call=%.2f" format call)

  case class CantBet(amount: Decimal, stack: Decimal)
    extends Error("Can't bet: got amount=%.2f, stack=%.2f" format (amount, stack))
}
