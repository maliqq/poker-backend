package de.pokerno.gameplay.discarding

import de.pokerno.model.seat
import de.pokerno.gameplay.Discarding

object NextTurn {
  
  def decide(all: Seq[seat.Sitting]): Discarding.Transition = {
    Discarding.Done
  }
  
}
