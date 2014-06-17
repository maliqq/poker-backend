package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty

case class Error(@JsonProperty message: String) extends GameEvent {
}
