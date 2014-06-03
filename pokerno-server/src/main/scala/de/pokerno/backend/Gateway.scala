package de.pokerno.backend

import de.pokerno.protocol.PlayerEvent
import de.pokerno.backend.gateway.http

object Gateway {
  case class Connect(conn: http.Connection)
  case class Disconnect(conn: http.Connection)
  case class Message(conn: http.Connection, msg: PlayerEvent)
}