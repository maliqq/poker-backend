package pokerno.backend.engine

import pokerno.backend.model._
import pokerno.backend.protocol._

trait Antes {
gameplay: Gameplay =>
  def postAntes {
    betting = new Betting.Context(table where(_ isActive))
    (0 to betting.items.size) foreach { _ =>
      forceBet(Bet.Ante)
      betting move
    }
  }
}
