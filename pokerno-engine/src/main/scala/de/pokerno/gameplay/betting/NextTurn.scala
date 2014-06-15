package de.pokerno.gameplay.betting

import concurrent.duration._
import math.{BigDecimal => Decimal}
import de.pokerno.gameplay.{Betting, stg, Context => Gameplay, Round => GameplayRound}
import de.pokerno.util.Colored._

object NextTurn {
  import de.pokerno.model.seat.Sitting
  
  def decide(all: Seq[Sitting], call: Decimal): GameplayRound.Transition = {
    if (all.size < 2) {
      // no one to act, winner is last active player 
      return GameplayRound.Stop
    }

    val (_called, notCalled) = all.partition(_.isCalled(call))
    val (notChecked, called) = _called.partition(_.isPlaying)
    
    yellow("\t| call=%s\n\t| called: %s\n\t| notCalled: %s\n\t| notChecked: %s\n", call, called, notCalled, notChecked)

    if (called.nonEmpty) {
      notChecked.find(_.isPlaying) map { playing =>
        return (
            if (called.forall(_.isAllIn)) {
              warn("everyone goes all-in to %s (check)", playing.player)
              Betting.Showdown
            } else {
              warn("%s not checked yet", playing.player)
              GameplayRound.Require(playing)
            }
          )
      }
    }
    
    if (notCalled.isEmpty) {
      notChecked.headOption map { playing =>
        warn("everyone checked to %s", playing.player)
        return GameplayRound.Require(playing)
      }
      
      return (
          if (called.exists(_.isAllIn)) {
            warn("everyone called all-in")
            Betting.Showdown
          } else {
            warn("everyone called")
            GameplayRound.Done
          }
        )
    }
    
    val playing = notCalled.head
    
    if (notCalled.size == 1) {
      // one playing, others all-in
      if (called.nonEmpty && called.forall(_.isAllInTo(playing.putAmount))) {
        warn("everyone goes all-in to %s", playing.player)
        return Betting.Showdown
      }
    }
    
    // FIXME: everyone all-in
    GameplayRound.Require(playing)
  }
}
