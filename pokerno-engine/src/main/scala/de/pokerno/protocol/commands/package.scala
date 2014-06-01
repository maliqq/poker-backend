package de.pokerno.protocol

package object commands {
  type Decimal = math.BigDecimal
  type Player = de.pokerno.model.Player
  type Cards = Array[Byte]
  type Command = de.pokerno.protocol.Command
}
