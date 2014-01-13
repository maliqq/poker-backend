package de.pokerno.backend.gateway.http

import io.netty.channel.Channel

object Gateway {

  case class Connect(channel: Channel, connection: Connection)
  case class Disconnect(channel: Channel)
  case class Message(channel: Channel, message: String)

}
