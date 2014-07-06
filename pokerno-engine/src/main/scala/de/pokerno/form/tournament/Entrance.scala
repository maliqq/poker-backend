package de.pokerno.form.tournament

import de.pokerno.model.tournament.Entry

/*
Входы в турнир
*/
trait Entrance {
  val ctx: Context
  val entries = collection.mutable.Map.empty[Player, Entry]
  
  def enter(player: Player) {
    entries += (player -> Entry(ctx.startingStack))
  }
  
  def leave(player: Player) {
    entries.remove(player)
  }
  
}
