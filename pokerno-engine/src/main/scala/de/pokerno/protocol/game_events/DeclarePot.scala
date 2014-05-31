package de.pokerno.protocol.game_events

import beans._

sealed case class DeclarePot(
    @BeanProperty var pot: Decimal,

    @BeanProperty var side: Seq[Decimal] = Seq(),

    @BeanProperty var rake: Option[Decimal] = None
  ) extends GameEvent {}
