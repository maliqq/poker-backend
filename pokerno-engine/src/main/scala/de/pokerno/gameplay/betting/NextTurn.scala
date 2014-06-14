package de.pokerno.gameplay.betting

import concurrent.duration._
import math.{BigDecimal => Decimal}
import de.pokerno.gameplay.{Betting, stg, Context => Gameplay}
import de.pokerno.util.Colored._

object NextTurn {
  import de.pokerno.model.seat.Sitting
  
  def decide(all: Seq[Sitting], call: Decimal): Betting.Transition = {
    if (all.size < 2) {
      // no one to act, winner is last active player 
      return Betting.Stop
    }

    val (_called, notCalled) = all.partition(_.isCalled(call))
    val (notChecked, called) = _called.partition(_.isPlaying)
    
    yellow("\t| call=%s\n\t| called: %s\n\t| notCalled: %s\n\t| notChecked: %s\n", call, called, notCalled, notChecked)

    notChecked.find(_.isPlaying) map { playing =>
      return (
          if (called.forall(_.isAllIn))
            Betting.Showdown
          else
            Betting.Require(playing)
        )
    }
    
    if (notCalled.isEmpty) {
      return (
          if (called.exists(_.isAllIn))
            Betting.Showdown
          else
            Betting.Done
        )
    }
    
    val playing = notCalled.head
    
    if (notCalled.size == 1) {
      // one playing, others all-in
      if (called.forall { s =>
          s.isAllIn && playing.putAmount >= s.putAmount
      }) return Betting.Showdown
    }
    
    // FIXME: everyone all-in
    Betting.Require(playing)
  }
}

trait NextTurn {

  val gameplay: Gameplay

  import gameplay._
  
  def nextTurn(): Betting.Transition = NextTurn.decide(round.seats.filter(_.inPot), round.callAmount)

}
