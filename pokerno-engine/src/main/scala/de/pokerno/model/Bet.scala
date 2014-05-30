package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import java.util.Locale

case class Bet(val betType: Bet.Value, val amount: Option[Decimal] = None, val timeout: Option[Boolean] = None) {
  override def toString = {
    if (amount.isDefined)
      "%s %.2f" formatLocal (Locale.US, betType.toString, amount.get)
    else betType.toString
  }
  
  def isActive =  amount.isDefined && amount.get > 0
  def isForced =  betType.isInstanceOf[Bet.ForcedBet]
  def isRaise =   betType == Bet.Raise
  def isCheck =   betType == Bet.Check
  def isCall =    betType == Bet.Call
  def isFold =    betType == Bet.Fold

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

  abstract class ForcedBet  extends Value
  case object SmallBlind    extends ForcedBet with Rateable
  case object BigBlind      extends ForcedBet with Rateable
  case object Ante          extends ForcedBet with Rateable
  case object BringIn       extends ForcedBet with Rateable
  case object GuestBlind    extends ForcedBet
  case object Straddle      extends ForcedBet

  abstract class PassiveBet extends Value
  case object Fold          extends PassiveBet
  case object Check         extends PassiveBet

  abstract class ActiveBet  extends Value
  case object Raise         extends ActiveBet
  case object AllIn         extends ActiveBet
  case object Call          extends ActiveBet

  abstract class CardAction extends Value

  def check()                   = Bet(Check)
  def check(timeout: Boolean)   = Bet(Check, timeout = Some(timeout))

  def fold()                    = Bet(Fold)
  def fold(timeout: Boolean)    = Bet(Fold, timeout = Some(timeout))

  def call(amount: Decimal)     = Bet(Call, Some(amount))
  def raise(amount: Decimal)    = Bet(Raise, Some(amount))
  def allin()                   = Bet(AllIn)

  def forced(t: ForcedBet,
      amount: Decimal)          = Bet(t, Some(amount))
  def sb(amount: Decimal)       = forced(SmallBlind, amount)
  def bb(amount: Decimal)       = forced(BigBlind, amount)
  def ante(amount: Decimal)     = forced(Ante, amount)

  case class CantCheck(call: Decimal)
    extends Error("Can't check: need to call=%.2f" format call)

  case class CantBet(amount: Decimal, stack: Decimal)
    extends Error("Can't bet: got amount=%.2f, stack=%.2f" format (amount, stack))
}
