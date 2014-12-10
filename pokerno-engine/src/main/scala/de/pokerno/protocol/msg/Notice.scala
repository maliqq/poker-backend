package de.pokerno.protocol.msg

import com.fasterxml.jackson.annotation.JsonProperty

case class Notice(@JsonProperty message: String) extends GameEvent {
}
