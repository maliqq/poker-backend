package de.pokerno.protocol.game_events

import beans._
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed case class StakeChange(
    @BeanProperty bigBlind: Decimal,
    @BeanProperty smallBlind: Option[Decimal] = None,
    @BeanProperty ante: Option[Decimal] = None,
    @BeanProperty bringIn: Option[Decimal] = None
) extends GameEvent {}
