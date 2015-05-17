package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty

case class Chat(@JsonProperty player: Player, @JsonProperty message: String) extends GameEvent {
}
