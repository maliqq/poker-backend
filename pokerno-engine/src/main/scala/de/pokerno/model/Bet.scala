package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import java.util.Locale

import beans._

object BetType extends Enumeration {
  private def value(s: String) = new Val(nextId, s)

  val SmallBlind  = value("small-blind")
  val BigBlind    = value("big-blind")
  val Ante        = value("ante")
  val BringIn     = value("bring-in")
  val GuestBlind  = value("guest-blind")
  val Straddle    = value("straddle")

  val AllIn       = value("all-in")
  
  val Call        = value("call")
  val Raise       = value("raise")
  val Check       = value("check")
  val Fold        = value("fold")
  
  final val FORCED = Seq(SmallBlind, BigBlind, Ante, BringIn, GuestBlind, Straddle)
}

case class Bet(
    `type`: BetType.Value,
    amount: Option[Decimal] = None
  ) {
  
  def betType = `type`
  
  override def toString = {
    if (amount.isDefined)
      "%s %.2f" formatLocal (Locale.US, betType.toString, amount.get)
    else betType.toString
  }
  
  def isActive =  amount.isDefined && amount.get > 0
  def isForced =  BetType.FORCED.contains(betType)
  def isRaise =   betType == BetType.Raise
  def isCheck =   betType == BetType.Check
  def isCall =    betType == BetType.Call
  def isFold =    betType == BetType.Fold

  override def equals(other: Any): Boolean = {
    other match {
      case b: Bet ⇒ betType == b.betType && amount == b.amount
      case _      ⇒ false
    }
  }
}

object Bet {
  import BetType._

  def check()                   = Bet(Check)

  def fold()                    = Bet(Fold)

  def call(amount: Decimal)     = Bet(Call, Some(amount))
  def raise(amount: Decimal)    = Bet(Raise, Some(amount))
  def allIn()                   = Bet(AllIn)
//
//  def forced(t: Value, amount: Decimal) =
//        if (BetType.FORCED.contains(t)) Bet(t, Some(amount))
//        else throw new IllegalArgumentException("bet should be one of: %s" format (BetType.FORCED.map(_.toString).mkString(", ")))
  
  def sb(amount: Decimal)       = Bet(SmallBlind, Some(amount))
  def bb(amount: Decimal)       = Bet(BigBlind, Some(amount))
  def ante(amount: Decimal)     = Bet(Ante, Some(amount))
  def bringIn(amount: Decimal)  = Bet(BringIn, Some(amount))
  def gb(amount: Decimal)       = Bet(GuestBlind, Some(amount))
  def straddle(amount: Decimal) = Bet(Straddle, Some(amount))

  case class CantCheck(call: Decimal)
    extends Error("Can't check: need to call=%.2f" format call)

  case class CantBet(amount: Decimal, stack: Decimal)
    extends Error("Can't bet: got amount=%.2f, stack=%.2f" format (amount, stack))
}
