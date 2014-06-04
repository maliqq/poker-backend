package de.pokerno.protocol

package object msg {

  type Decimal      = math.BigDecimal
  type Player       = String
  type Cards        = de.pokerno.poker.Cards
  type GameEvent    = de.pokerno.protocol.GameEvent
  type Cards2Binary = de.pokerno.protocol.Serializers.Cards2Binary
  
}
