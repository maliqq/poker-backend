package de.pokerno.model

import scala.math.{ BigDecimal ⇒ Decimal }
import java.util.Locale
import de.pokerno.backend.{protocol => proto}

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
  type Value = proto.BetSchema.BetType
  
  final val Call: Value = proto.BetSchema.BetType.CALL
  final val Raise: Value = proto.BetSchema.BetType.RAISE
  final val Check: Value = proto.BetSchema.BetType.CHECK
  final val Fold: Value = proto.BetSchema.BetType.FOLD
  final val Ante: Value = proto.BetSchema.BetType.ANTE
  final val BringIn: Value = proto.BetSchema.BetType.BRING_IN
  final val SmallBlind: Value = proto.BetSchema.BetType.SB
  final val BigBlind: Value = proto.BetSchema.BetType.BB
  final val GuestBlind: Value = proto.BetSchema.BetType.GUEST_BLIND
  final val Straddle: Value = proto.BetSchema.BetType.STRADDLE

  // TODO
  final val Discard = proto.DiscardCardsSchema.DiscardType.DISCARD
  final val StandPat = proto.DiscardCardsSchema.DiscardType.STAND_PAT
  
  // TODO
  final val Show = proto.ShowCardsSchema.ShowType.SHOW
  final val Muck = proto.ShowCardsSchema.ShowType.MUCK

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
