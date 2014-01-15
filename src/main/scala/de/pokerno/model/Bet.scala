package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import java.util.Locale

class Bet(val betType: Bet.Value, val amount: Decimal = .0) {
  override def toString =
    if (amount > .0) "%s %.2f" formatLocal (Locale.US, betType toString, amount)
    else betType toString

  def isValid(left: Decimal, _put: Decimal, call: Decimal, _range: Range): Boolean = betType match {
    case Bet.Fold ⇒
      true

    case Bet.Check ⇒
      call == _put

    case Bet.Call | Bet.Raise ⇒
      val stack = left + _put
      val range = if (betType == Bet.Call) Range(call, call) else _range
      amount <= stack && range.isValid(amount, stack)

    case _ ⇒
      if (isForced)
        amount == call || (amount < call && amount == left)
      else
        false
  }
  
  def isForced: Boolean = betType match {
    case Bet.Ante | Bet.BringIn | Bet.SmallBlind | Bet.BigBlind | Bet.GuestBlind | Bet.Straddle =>
      true
    case _ => false
  }
  
  override def equals(other: Any): Boolean = {
    other match {
      case b: Bet => betType == b.betType && amount == b.amount
      case _ => false
    }
  }
}

object Bet {
  import de.pokerno.protocol.{wire, msg}
  type Value = wire.BetSchema.BetType
  
  final val Call: Value = wire.BetSchema.BetType.CALL
  final val Raise: Value = wire.BetSchema.BetType.RAISE
  final val Check: Value = wire.BetSchema.BetType.CHECK
  final val Fold: Value = wire.BetSchema.BetType.FOLD
  final val Ante: Value = wire.BetSchema.BetType.ANTE
  final val BringIn: Value = wire.BetSchema.BetType.BRING_IN
  final val SmallBlind: Value = wire.BetSchema.BetType.SB
  final val BigBlind: Value = wire.BetSchema.BetType.BB
  final val GuestBlind: Value = wire.BetSchema.BetType.GUEST_BLIND
  final val Straddle: Value = wire.BetSchema.BetType.STRADDLE

  // TODO
  final val Discard = msg.DiscardCardsSchema.DiscardType.DISCARD
  final val StandPat = msg.DiscardCardsSchema.DiscardType.STAND_PAT
  
  // TODO
  final val Show = msg.ShowCardsSchema.ShowType.SHOW
  final val Muck = msg.ShowCardsSchema.ShowType.MUCK

  def check = new Bet(Check)
  def fold = new Bet(Fold)
  def call(amount: Decimal) = new Bet(Call, amount)
  def raise(amount: Decimal) = new Bet(Raise, amount)
  def forced(t: Value, amount: Decimal) = new Bet(t, amount)

  case class CantCheck(call: Decimal)
    extends Error("Can't check: need to call=%.2f" format (call))

  case class CantBet(amount: Decimal, stack: Decimal)
    extends Error("Can't bet: got amount=%.2f, stack=%.2f" format (amount, stack))
}
