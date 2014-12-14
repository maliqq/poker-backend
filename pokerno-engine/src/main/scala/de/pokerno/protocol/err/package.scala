package de.pokerno.protocol

package object err {
  type Player = String
  type GameEvent    = de.pokerno.protocol.GameEvent

  class Err(@JsonProperty val code: String) {
    @JsonProperty val message: String
    @JsonProperty val payload: Map[String, Any] = Map.empty
  }
}
