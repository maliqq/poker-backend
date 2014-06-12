package de.pokerno.gameplay.betting

import concurrent.duration._

import de.pokerno.model.Seat
import de.pokerno.gameplay.{Betting, stg, Context => Gameplay}
import de.pokerno.util.Colored._

trait NextTurn {

  val gameplay: Gameplay

  import gameplay._
  
  def nextTurn(): Betting.Transition = {
    // PLAY | BET | ALL_IN
    val inpot = round.seats.filter(_.inPot)
    val isHeadsup = inpot.size == 2
    
    error("callAmount=%s allInAmount=%s inpot=%s isheadsup=%s", round.callAmount, round.allInAmount, inpot, isHeadsup)
    
    if (inpot.size < 2) {
      // no one to act, winner is active player 
      return Betting.Stop
    }
    
    val called = inpot.count(_.isCalled(round.callAmount))
    if (called == inpot.size) {
      // everyone called
      return Betting.Done
    }
    
    // PLAY | BET
    val inplay = inpot.filter (_.inPlay)
    error("inplay=%s", inplay)
    if (inplay.size >= 2) {
      // ask for next bet
      return Betting.Require(inplay.head)
    }
    
    // one playing, not called all-in
    if (inplay.size == 1) {
      val playing = inplay.head
      if (inpot.exists (_.isAllIn) && !playing.isCalled(round.allInAmount)) {
        error("playing=%s not called=%s", playing, round.allInAmount)
        return Betting.Require(playing)
      }
    }
    
    // everyone all-in, or one playing, others all-in
    return Betting.Showdown
  }

}
