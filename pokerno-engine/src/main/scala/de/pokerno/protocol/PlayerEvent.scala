package de.pokerno.protocol

import de.pokerno.protocol.{action => message}
import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonSubTypes, JsonIgnoreProperties}

object PlayerEvent extends Codec.Json {
  def decode(data: Array[Byte]): PlayerEvent = decode[PlayerEvent](data)
  def decode(data: String): PlayerEvent = decodeFromString[PlayerEvent](data)
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes(Array(
  new JsonSubTypes.Type(name = "join",      value = classOf[message.JoinTable]),
  new JsonSubTypes.Type(name = "leave",     value = classOf[message.LeaveTable]),
  new JsonSubTypes.Type(name = "sitout",    value = classOf[message.SitOut]),
  new JsonSubTypes.Type(name = "comeback",  value = classOf[message.ComeBack]),
  new JsonSubTypes.Type(name = "bet",       value = classOf[message.AddBet]),
  new JsonSubTypes.Type(name = "buyin",     value = classOf[message.BuyIn]),
  new JsonSubTypes.Type(name = "rebuy",     value = classOf[message.Rebuy]),
  new JsonSubTypes.Type(name = "discard",   value = classOf[message.DiscardCards]),
  new JsonSubTypes.Type(name = "show",      value = classOf[message.ShowCards]),
  new JsonSubTypes.Type(name = "chat",      value = classOf[message.Chat])
))
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class PlayerEvent extends Message {}
