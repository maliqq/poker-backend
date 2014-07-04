package de.pokerno.gameplay.tournament

import de.pokerno.model.tournament.Entry

trait Knockout {
  val ctx: Context
  import ctx._

  def knockout(winner: Player, loser: Player) {
    val entry = entries(winner)
    entry.knockoutsCount += 1
  }
  
}
