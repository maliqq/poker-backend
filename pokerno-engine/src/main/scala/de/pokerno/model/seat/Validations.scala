package de.pokerno.model.seat

import de.pokerno.model.{Seat, Bet, Stake}
import math.{BigDecimal => Decimal}
import de.pokerno.util.Colored._

trait Validations { seat: Sitting =>
  
  // CHECK
  def canCheck(toCall: Decimal) = putAmount == toCall
  
  // FOLD
  def canFold = inPlay
  
  // FORCED BET
  def canForce(amt: Decimal, toCall: Decimal) =
    // TODO
    inPlay && _canCall(amt, toCall)
  
  // RAISE
  def canRaise(amt: Decimal): Boolean = {
    inPlay && _raise.map(_canRaise(amt, _)).getOrElse(false)
  }

  private def _canRaise(amt: Decimal, toRaise: Tuple2[Decimal, Decimal]) = {
    amt <= total && amt >= toRaise._1 && amt <= toRaise._2
  }
  
  // CALL
  def canCall(amt: Decimal) = {
    inPlay &&
      _call.map(_canCall(amt, _)).getOrElse(false)
  }
  
  def _canCall(amt: Decimal, toCall: Decimal) = {
    // call all-in
    amt == stackAmount && amt + putAmount < toCall ||
    // call exact amount
    amt <= stackAmount && amt + putAmount == toCall
  }
  
  def isCalled(amt: Decimal) =
    (isAllIn && putAmount <= amt) || putAmount >= amt
  
  def isAllInTo(amt: Decimal) =
    isAllIn && putAmount <= amt
  
  def canBet =
    inPlay || isPostingBB
  
  def canBet(bet: Bet, stake: Stake): Boolean =
    bet match {
      case Bet.Fold ⇒
        canFold || notActive
  
      case Bet.Check ⇒
        _call.map(canCheck(_)).getOrElse(true)
  
      // FIXME check on null
      case Bet.Call(amt) if _call.isDefined && isActive ⇒
        canCall(amt)
  
      // FIXME check on null
      case Bet.Raise(amt) if _raise.isDefined && isActive ⇒
        canRaise(amt)
  
      case f: Bet.Forced ⇒
        canForce(f.amount, stake.amount(f.betType))
  
      case _ ⇒
        // TODO warn
        false
    }

  def canLeave = notActive
  
}
