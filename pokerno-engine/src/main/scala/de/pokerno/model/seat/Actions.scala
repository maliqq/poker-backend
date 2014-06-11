package de.pokerno.model.seat

import de.pokerno.model.{Seat, Bet}
import math.{BigDecimal => Decimal}

trait Actions { s: Seat =>
  import Seat.State
  
  def check(): Decimal = {
    _state = State.Bet
    _action = Some(Bet.Check)
    .0
  }
  
  def fold(): Decimal = {
    _state = State.Fold
    _put = None
    _action = Some(Bet.Fold)
    .0
  }

  def call(amt: Decimal): Decimal = {
    put(amt, State.Bet)
    _action = Some(Bet.Call(amt))
    amt
  }

  def force(bet: Bet.Forced): Decimal = {
    val amt = bet.amount
    put(amt, State.Play)
    _action = Some(bet)
    amt
  }
  
  def raise(amt: Decimal): Decimal = {
    val diff = amt - putAmount
    put(diff, State.Bet)
    _action = Some(Bet.Raise(amt))
    diff
  }
  
  def postBet(bet: Bet): Decimal =
    bet match {
      case Bet.Fold                         ⇒ fold
      case Bet.Raise(amt) if amt > 0        ⇒ raise(amt)
      case Bet.Call(amt) if amt > 0         ⇒ call(amt)
      case Bet.Check                        ⇒ check()
      case f: Bet.Forced if f.amount > 0    ⇒ force(f)
      case x ⇒
        log.warn("unhandled postBet: {}", x)
        0
    }
  
}
