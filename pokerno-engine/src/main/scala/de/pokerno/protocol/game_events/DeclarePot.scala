package de.pokerno.protocol.game_events

import beans._
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class DeclarePot(
    @BeanProperty var pot: Decimal,

    @BeanProperty var side: Seq[Decimal] = Seq(),

    @BeanProperty var rake: Option[Decimal] = None
  ) extends GameEvent {}
