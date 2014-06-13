package de.pokerno.model.seat

import de.pokerno.model.{Seat, Bet, Stake}
import math.{BigDecimal => Decimal}
import de.pokerno.util.Colored._

trait Validations { seat: Sitting =>
  
  // CHECK
  def canCheck(toCall: Decimal): Boolean = {
    putAmount == toCall
  }
  
  // FOLD
  def canFold: Boolean = {
    inPlay
  }
  
  // FORCED BET
  def canForce(amt: Decimal, toCall: Decimal): Boolean = {
    // TODO
    inPlay && _canCall(amt, toCall)
  }
  
  // RAISE
  def canRaise(amt: Decimal): Boolean = {
    inPlay && _raise.map(_canRaise(amt, _)).getOrElse(false)
  }

  private def _canRaise(amt: Decimal, toRaise: Tuple2[Decimal, Decimal]): Boolean = {
    amt <= total && amt >= toRaise._1 && amt <= toRaise._2
  }
  
  // CALL
  def canCall(amt: Decimal): Boolean = {
    inPlay &&
      _call.map(_canCall(amt, _)).getOrElse(false)
  }
  
  def _canCall(amt: Decimal, toCall: Decimal) = {
    // call all-in
    amt == stackAmount && amt + putAmount < toCall ||
    // call exact amount
    amt <= stackAmount && amt + putAmount == toCall
  }
  
  def isCalled(amt: Decimal): Boolean = {
    isAllIn || _isCalled(amt)
  }
  
  private def _isCalled(amt: Decimal): Boolean = {
    amt <= putAmount
  }
  
  def canBet: Boolean = {
    inPlay || isPostingBB
  }
  
  def canBet(bet: Bet, stake: Stake): Boolean =
    bet match {
      case Bet.Fold ⇒
        canFold || notActive
  
      case Bet.Check ⇒
        _call.isEmpty || canCheck(_call.get)
  
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
