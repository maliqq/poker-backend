package de.pokerno.backend.gateway.http

object Gateway {
  case class Connect(conn: Connection)
  case class Disconnect(conn: Connection)
  case class Message(conn: Connection, message: String)
}
