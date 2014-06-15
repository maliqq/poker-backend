package de.pokerno.gameplay.discarding

import de.pokerno.model.seat
import de.pokerno.gameplay.{Round => GameplayRound}

object NextTurn {
  
  def decide(all: Seq[seat.Sitting]): GameplayRound.Transition = {
    // TODO
    GameplayRound.Done
  }
  
}
