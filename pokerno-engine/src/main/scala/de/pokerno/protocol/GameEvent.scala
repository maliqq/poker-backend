package de.pokerno.protocol

import de.pokerno.protocol.{game_events => message}
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonIgnoreProperties}

object GameEvent extends Codec.Json {
  def encode(e: GameEvent) = encodeAsBytes(e)
  
  def encodeAsBytes(e: GameEvent): Array[Byte] =
    mapper.writeValueAsBytes(e)

  def encodeAsString(e: GameEvent): String =
    mapper.writeValueAsString(e)
}

@JsonSubTypes(Array(
  new JsonSubTypes.Type(name = "bet:add",      value = classOf[message.DeclareBet]),
  // new JsonSubTypes.Type(name = "bet:fold",      value = classOf[message.DeclareFold]),
  // new JsonSubTypes.Type(name = "bet:check",     value = classOf[message.DeclareCheck]),
  // new JsonSubTypes.Type(name = "bet:call",      value = classOf[message.DeclareCall]),
  // new JsonSubTypes.Type(name = "bet:raise",     value = classOf[message.DeclareRaise]),
  // new JsonSubTypes.Type(name = "bet:ante",      value = classOf[message.DeclareAnte]),
  // new JsonSubTypes.Type(name = "bet:bringin",   value = classOf[message.DeclareBringIn]),
  // new JsonSubTypes.Type(name = "bet:sb",        value = classOf[message.DeclareSmallBlind]),
  // new JsonSubTypes.Type(name = "bet:bb",        value = classOf[message.DeclareBigBlind]),
  // new JsonSubTypes.Type(name = "bet:gb",        value = classOf[message.DeclareGuestBlind]),
  // new JsonSubTypes.Type(name = "bet:straddle",  value = classOf[message.DeclareStraddle]),
  
  new JsonSubTypes.Type(name = "cards:show",    value = classOf[message.ShowCards]),
  new JsonSubTypes.Type(name = "cards:discard", value = classOf[message.DiscardCards]),

  new JsonSubTypes.Type(name = "cards:board",   value = classOf[message.DealBoard]),
  new JsonSubTypes.Type(name = "cards:hole",    value = classOf[message.DealHole]),
  new JsonSubTypes.Type(name = "cards:door",    value = classOf[message.DealDoor]),
  
  new JsonSubTypes.Type(name = "bet:ask",       value = classOf[message.AskBet]),
  new JsonSubTypes.Type(name = "discard:ask",   value = classOf[message.AskDiscard]),
  new JsonSubTypes.Type(name = "pot:",          value = classOf[message.DeclarePot]),
  new JsonSubTypes.Type(name = "hand:",         value = classOf[message.DeclareHand]),
  new JsonSubTypes.Type(name = "winner:",       value = classOf[message.DeclareWinner]),
  new JsonSubTypes.Type(name = "timer:tick",    value = classOf[message.TickTimer]),

  //new JsonSubTypes.Type(name = "seat:", value = classOf[DeclareSeat]),

  new JsonSubTypes.Type(name = "start:",        value = classOf[message.DeclareStart]),
  new JsonSubTypes.Type(name = "table:button",  value = classOf[message.ButtonChange]),
  new JsonSubTypes.Type(name = "game:",         value = classOf[message.GameChange]),
  new JsonSubTypes.Type(name = "stake:",        value = classOf[message.StakeChange]),
  new JsonSubTypes.Type(name = "stage:",        value = classOf[message.DeclarePlayStart]),
  new JsonSubTypes.Type(name = "stage:",        value = classOf[message.DeclarePlayStop]),
  new JsonSubTypes.Type(name = "stage:",        value = classOf[message.DeclareStreet]),

  new JsonSubTypes.Type(name = "player:join",   value = classOf[message.PlayerJoin]),
  new JsonSubTypes.Type(name = "player:online",   value = classOf[message.PlayerOnline]),
  new JsonSubTypes.Type(name = "player:offline",   value = classOf[message.PlayerOffline]),
  new JsonSubTypes.Type(name = "player:sitout",   value = classOf[message.PlayerSitOut]),
  new JsonSubTypes.Type(name = "player:comeback",   value = classOf[message.PlayerComeBack]),
  new JsonSubTypes.Type(name = "player:leave",  value = classOf[message.PlayerLeave])
))
abstract class GameEvent extends Message {}
