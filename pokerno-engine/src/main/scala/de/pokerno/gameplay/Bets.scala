package de.pokerno.gameplay

import de.pokerno.model.Bet

private[gameplay] trait Bets {
  def ctx: StageContext
  def round: betting.Round
  
  // require bet
  def requireBet(pos: Int) {
    val seat = round requireBet pos
    val player = seat.player.get
    ctx broadcast Event.requireBet(pos, player, round.call, round.raise)
  }

  // add bet
  def addBet(bet: Bet) {
    val (seat, posted) = round.addBet(bet)
    val pos = round.current
    val player = seat.player.get
    ctx broadcast Event.addBet(pos, player, posted)
  }

  // force bet
  def forceBet(pos: Int, _type: Bet.ForcedBet) {
    val (seat, posted) = round.forceBet(pos, _type)
    val pos = round.current
    val player = seat.player.get
    ctx broadcast Event.addBet(pos, player, posted)
  }

  // current betting round finished
  def doneBets() {
    ctx broadcast Event.declarePot(round.pot.total,
        round.pot.sidePots.map(_.total))
    round complete()
  }
}
