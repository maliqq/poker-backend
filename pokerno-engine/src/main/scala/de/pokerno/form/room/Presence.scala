package de.pokerno.form.room

import de.pokerno.model.Player

trait Presence {

  def playerOffline(player: Player)
  def playerAway(player: Player)
  def playerOnline(player: Player)
  
}
