package de.pokerno.model.seat

import de.pokerno.model.{Seat, Bet}

trait Actions { s: Sitting =>
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
    puts(amt)
    betting()
    _action = Some(Bet.Call(amt))
    amt
  }

  def force(bet: Bet.Forced): Decimal = {
    val amt = bet.amount
    puts(amt)
    playing()
    _action = Some(bet)
    amt
  }
  
  def raise(amt: Decimal): Decimal = {
    val diff = amt - putAmount
    puts(diff)
    betting()
    _action = Some(Bet.Raise(amt))
    diff
  }
  
  def posting(bet: Bet): Bet = bet match {
    case Bet.AllIn =>
      Bet.raise(total)
    case Bet.Call(amt) if amt == null || amt == 0 =>
      Bet.Call(List(callAmount, total).min - putAmount)
    case _ =>
      bet
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
