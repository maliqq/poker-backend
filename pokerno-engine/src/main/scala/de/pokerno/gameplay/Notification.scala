package de.pokerno.gameplay

import de.pokerno.hub
import de.pokerno.protocol.GameEvent

case class Notification(
    payload: GameEvent,
    from: String,
    to: Destination,
    at: java.time.Instant = java.time.Instant.now()
  )
