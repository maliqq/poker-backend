package pokerno.backend.engine

import pokerno.backend.protocol._

trait BringIn {
  g: Gameplay ⇒
  
  def bringIn {
    val (seat, pos) = round.seats where (_ isActive) minBy { case (seat, pos) ⇒
      dealer pocket (seat.player get) last
    }
    setButton(pos)
    round.acting = (seat, pos)
    
    betting ! Betting.Require(stake.bringIn get, game.limit)
  }
}
