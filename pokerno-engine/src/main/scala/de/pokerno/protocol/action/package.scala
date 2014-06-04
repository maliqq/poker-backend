package de.pokerno.protocol

package object action {
  type Decimal = math.BigDecimal
  type Cards = Array[Byte]
  type Player = de.pokerno.model.Player
  type PlayerEvent = de.pokerno.protocol.PlayerEvent
}
