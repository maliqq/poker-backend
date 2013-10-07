package pokerno.backend.engine

import pokerno.backend.protocol._

trait BringIn {
  g: Gameplay ⇒
  def bringIn {
    val active = table.seats where (_ isActive)
    val (seat, pos) = active minBy {
      case (seat, pos) ⇒
        val pocketCards = dealer pocket (seat.player get)
        pocketCards last
    }
    setButton(pos)

    betting current = active.head
    requireBet
  }
}
