package de.pokerno.gameplay

import de.pokerno.model.Bet

trait Bets {
  val ctx: stg.Context
  
  def round: betting.Round = ctx.gameplay.round
  
  // require bet
  def requireBet(pos: Int) {
    val seat = round requireBet pos
    val player = seat.player.get
    ctx broadcast Events.requireBet(pos, player, round.call, round.raise)
  }

  // add bet
  def addBet(bet: Bet) {
    val (seat, posted) = round.addBet(bet)
    val pos = round.current
    val player = seat.player.get
    ctx broadcast Events.addBet(pos, player, posted)
  }
  
  def addBetWithTimeout(bet: Bet) {
    val (seat, posted) = round.addBet(bet)
    val pos = round.current
    val player = seat.player.get
    val event = Events.addBet(pos, player, posted)
    event.timeout = Some(true)
    ctx broadcast event
  }

  // force bet
  def forceBet(pos: Int, betType: Bet.ForcedType) {
    val (seat, posted) = round.forceBet(pos, betType)
    val player = seat.player.get
    ctx broadcast Events.addBet(pos, player, posted)
  }

  // current betting round finished
  def doneBets() {
    ctx broadcast Events.declarePot(round.pot.total,
        round.pot.sidePots.map(_.total))
    round complete()
  }
}
