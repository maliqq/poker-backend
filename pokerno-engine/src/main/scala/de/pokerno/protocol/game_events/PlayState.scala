package de.pokerno.protocol.game_events

import beans._
import de.pokerno.gameplay.Context
import de.pokerno.model.Street

class PlayState(
    gameplay: Context,
    @BeanProperty val street: Option[Street.Value] = None
) {
  @BeanProperty var id: String
  @BeanProperty var started: Long
  @BeanProperty var ended: Option[Long] = None
  
  @BeanProperty var board: Cards = gameplay.dealer.board
  @BeanProperty var pot: Decimal = gameplay.round.pot.total
  @BeanProperty var rake: Decimal = 0
  
  @BeanProperty var winners: Map[Player, Decimal] = Map.empty
  @BeanProperty var pockets: Map[Player, Cards] = Map.empty
}
