package de.pokerno.protocol.game_events

import beans._
import de.pokerno.model.MinMax

sealed case class AskBet(
    @BeanProperty var pos: Int,

    @BeanProperty var player: Player,

    @BeanProperty var call: Decimal,

    @BeanProperty var raise: MinMax[Decimal]
  ) extends GameEvent {}
