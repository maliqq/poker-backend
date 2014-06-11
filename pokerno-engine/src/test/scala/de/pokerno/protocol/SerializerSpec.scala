package de.pokerno.protocol

import org.scalatest._
import org.scalatest.Matchers._

class SerializerSpec extends FunSpec {

  import de.pokerno.poker.Cards
  import com.fasterxml.jackson.annotation.JsonUnwrapped
  import com.fasterxml.jackson.databind.annotation.JsonSerialize
  
  object Json extends Codec.Json {
  }
  
  case class CardsWrapper(
      @JsonSerialize(converter = classOf[Serializers.Cards2Binary])
      @JsonUnwrapped
      cards: Cards
    ) {}

  describe("Cards") {
    it("serialize") {
      val c = CardsWrapper(Cards.fromString("AhAd"))
      Json.encode(c) should equal("MjM=")
    }
  }
}
