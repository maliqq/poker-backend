package de.pokerno.gameplay

import de.pokerno.model.{Bet, BetType}

private[gameplay] trait Bets {
  val ctx: StageContext
  
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
  def forceBet(pos: Int, _type: BetType.Value) {
    val (seat, posted) = round.forceBet(pos, _type)
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
