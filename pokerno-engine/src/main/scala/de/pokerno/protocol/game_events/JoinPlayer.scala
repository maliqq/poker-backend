package de.pokerno.protocol.game_events

import beans._

sealed case class JoinPlayer(
    @BeanProperty var pos: Int,
    @BeanProperty var player: Player,
    @BeanProperty var amount: Decimal
) extends GameEvent {}
