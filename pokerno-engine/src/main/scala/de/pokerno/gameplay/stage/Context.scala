package de.pokerno.gameplay.stage

import akka.actor.ActorRef
import de.pokerno.protocol.GameEvent
import de.pokerno.gameplay.{ Context => Gameplay}

private[gameplay] class Context(
    val gameplay: Gameplay,
    val ref: ActorRef
  )
