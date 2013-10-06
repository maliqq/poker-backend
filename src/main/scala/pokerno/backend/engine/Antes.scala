package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

trait Antes {
  g: Gameplay ⇒
  def postAntes = if (game.options.hasAnte && stake.ante.isDefined) {
    val round = table.seats where (_ isActive)
    
    round foreach { case (seat, pos) ⇒
      betting current = (seat, pos)
      
      forceBet(Bet.Ante)
    }
    
    completeBetting
  }
}
