package de.pokerno.protocol

import de.pokerno.poker.Cards

object Serializers {
  class Cards2Binary extends com.fasterxml.jackson.databind.util.StdConverter[Cards, Array[Byte]] {
    override def convert(c: Cards): Array[Byte] = c
  }
}
