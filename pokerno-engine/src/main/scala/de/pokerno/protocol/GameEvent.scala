package de.pokerno.protocol

import de.pokerno.protocol.{game_events => message}
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonIgnoreProperties}

@JsonSubTypes(Array(
  new JsonSubTypes.Type(name = "bet:add",       value = classOf[message.DeclareBet]),
  new JsonSubTypes.Type(name = "cards:show",    value = classOf[message.ShowCards]),
  new JsonSubTypes.Type(name = "cards:discard", value = classOf[message.DiscardCards]),

  new JsonSubTypes.Type(name = "cards:deal",    value = classOf[message.DealCards]),
  new JsonSubTypes.Type(name = "bet:ask",       value = classOf[message.AskBet]),
  new JsonSubTypes.Type(name = "discard:ask",   value = classOf[message.AskDiscard]),
  new JsonSubTypes.Type(name = "pot:",          value = classOf[message.DeclarePot]),
  new JsonSubTypes.Type(name = "hand:",         value = classOf[message.DeclareHand]),
  new JsonSubTypes.Type(name = "winner:",       value = classOf[message.DeclareWinner]),
  new JsonSubTypes.Type(name = "timer:tick",    value = classOf[message.TickTimer]),

  //new JsonSubTypes.Type(name = "seat:", value = classOf[DeclareSeat]),

  new JsonSubTypes.Type(name = "start:",        value = classOf[message.DeclareStart]),
  new JsonSubTypes.Type(name = "table:",        value = classOf[message.ButtonChange]),
  new JsonSubTypes.Type(name = "gameplay:",     value = classOf[message.GameChange]),
  new JsonSubTypes.Type(name = "gameplay:",     value = classOf[message.StakeChange]),
  new JsonSubTypes.Type(name = "stage:",        value = classOf[message.DeclarePlayStart]),
  new JsonSubTypes.Type(name = "stage:",        value = classOf[message.DeclarePlayStop]),
  new JsonSubTypes.Type(name = "stage:",        value = classOf[message.DeclareStreet]),
  new JsonSubTypes.Type(name = "player:join",   value = classOf[message.JoinPlayer]),
  new JsonSubTypes.Type(name = "player:leave",  value = classOf[message.LeavePlayer])
))
abstract class GameEvent {}
