package de.pokerno.gameplay.tournament

import akka.actor.ActorRef
import de.pokerno.model._

trait Addons {
  
  val ctx: Context
  val rebuysAndAddonsManager: ActorRef
  
  def addon(player: Player) {
    
  }
  
  def isAddonBreak(): Boolean
  
}
