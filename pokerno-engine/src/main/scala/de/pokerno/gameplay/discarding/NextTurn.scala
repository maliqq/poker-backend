package de.pokerno.gameplay.discarding

import de.pokerno.model.seat.impl.Sitting
import de.pokerno.gameplay.{Round => GameplayRound}

private[gameplay] object NextTurn {
  
  def decide(all: Seq[Sitting]): GameplayRound.Transition = {
    // TODO
    all.find(_.isPlaying).map { playing =>
      return GameplayRound.Require(playing)
    }
    GameplayRound.Done
  }
  
}
