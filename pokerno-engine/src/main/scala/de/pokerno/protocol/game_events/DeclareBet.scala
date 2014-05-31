package de.pokerno.protocol.game_events

import beans._
import de.pokerno.model.Bet

sealed case class DeclareBet(
    @BeanProperty var pos: Int,
    
    @BeanProperty var player: Player,

    @BeanProperty var bet: Bet
  ) extends GameEvent {}
