package de.pokerno.protocol.msg

import de.pokerno.model.DealType

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped, JsonInclude, JsonIgnore}
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class DealCards(
    _cards: Either[Cards, Int]
) extends GameEvent {
  
    @JsonSerialize(converter = classOf[Cards2Binary])
    @JsonProperty val cards: Cards = _cards match {
      case Left(c) => c
      case _ => null
    }

    @JsonProperty val cardsNum: Option[Int] = _cards match {
      case Right(n) => Some(n)
      case _ => None
    }
}

import de.pokerno.model.Position

sealed case class DealHole(
    @JsonUnwrapped position: Position,
    @JsonIgnore _cards: Either[Cards, Int]
  ) extends DealCards(_cards)

sealed case class DealDoor(
    @JsonUnwrapped position: Position,
    @JsonIgnore _cards: Either[Cards, Int]    
  ) extends DealCards(_cards)

sealed case class DealBoard(
    @JsonIgnore _cards: Cards = null
) extends DealCards(Left(_cards))
