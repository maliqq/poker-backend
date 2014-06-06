package de.pokerno.protocol

import de.pokerno.protocol.{action => message}
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonIgnoreProperties}

object PlayerEvent extends Codec.Json {
  def decode(data: Array[Byte]): PlayerEvent = decode[PlayerEvent](data)
}

@JsonSubTypes(Array(
  new JsonSubTypes.Type(name = "table:join",      value = classOf[message.JoinTable]),
  new JsonSubTypes.Type(name = "table:leave",     value = classOf[message.LeaveTable]),
  new JsonSubTypes.Type(name = "bet:add",         value = classOf[message.AddBet]),
  new JsonSubTypes.Type(name = "cards:discard",   value = classOf[message.DiscardCards]),
  new JsonSubTypes.Type(name = "cards:show",      value = classOf[message.ShowCards])
))
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class PlayerEvent extends Message {}
