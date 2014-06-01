package de.pokerno.protocol.player_events

import beans._

sealed case class JoinTable(
    @BeanProperty var pos: Int,
    @BeanProperty var amount: Decimal
) extends PlayerEvent {}
