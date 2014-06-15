package de.pokerno.protocol.action

import com.fasterxml.jackson.annotation.{JsonValue, JsonCreator}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import de.pokerno.protocol.Serializers.Binary2Cards
import org.apache.commons.codec.binary.Base64

object DiscardCards {
  import de.pokerno.poker.Cards._
  
  @JsonCreator def build(b: String): DiscardCards = {
    DiscardCards(fromBinary(Base64.decodeBase64(b)))
  }
}

sealed case class DiscardCards(
  cards: Cards
) extends PlayerEvent {}
