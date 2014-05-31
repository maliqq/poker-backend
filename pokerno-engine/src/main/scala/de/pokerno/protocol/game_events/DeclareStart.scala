package de.pokerno.protocol.game_events

import beans._
import de.pokerno.model.{ Table, Variation, Stake }

sealed case class DeclareStart(

    @BeanProperty var table: Table,

    @BeanProperty var variation: Variation,

    @BeanProperty var stake: Stake//,
    // FIXME
    //@BeanProperty var play: PlayState

  ) extends GameEvent {}
