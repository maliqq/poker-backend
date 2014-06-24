package de.pokerno.backend

import de.pokerno.protocol.PlayerEvent
import de.pokerno.backend.gateway.{http, Http}

object Gateway extends Http.Events {
  
  case class Connect(conn: http.Connection) extends Http.Event.Connect
  case class Disconnect(conn: http.Connection) extends Http.Event.Disconnect
  case class Message(conn: http.Connection, msg: PlayerEvent) extends Http.Event.Message
  
  def connect(conn: http.Connection) = Connect(conn)
  
  def disconnect(conn: http.Connection) = Disconnect(conn)
  
  def message(conn: http.Connection, data: String) = {
    val msg = PlayerEvent.decode(data.getBytes)
    Message(conn, msg)
  }
  
}
