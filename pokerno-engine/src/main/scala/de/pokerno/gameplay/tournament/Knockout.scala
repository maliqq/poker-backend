package de.pokerno.gameplay.tournament

import de.pokerno.model.tournament.Entry

trait Knockout {
  val entries = collection.mutable.Map.empty[Player, Entry]

  def knockout(winner: Player, loser: Player) {
    val entry = entries(winner)
    entry.knockoutsCount += 1
  }
  
}
