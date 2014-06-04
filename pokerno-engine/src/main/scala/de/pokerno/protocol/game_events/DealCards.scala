package de.pokerno.protocol.game_events

import de.pokerno.model.DealType

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.{JsonInclude, JsonIgnore}

@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class DealCards(
    _cards: Either[Cards, Int]
) extends GameEvent {
  
    @JsonProperty val cards: Cards = _cards match {
      case Left(c) => c
      case _ => Array()
    }

    @JsonProperty val cardsNum: Option[Int] = _cards match {
      case Right(n) => Some(n)
      case _ => None
    }
}

sealed case class DealHole(
    @JsonProperty pos: Int,
    @JsonProperty player: Player,
    @JsonIgnore _cards: Either[Cards, Int]
  ) extends DealCards(_cards)

sealed case class DealDoor(
    @JsonProperty pos: Int,
    @JsonProperty player: Player,
    @JsonIgnore _cards: Either[Cards, Int]    
  ) extends DealCards(_cards)

sealed case class DealBoard(
    @JsonIgnore _cards: Cards = Array()
) extends DealCards(Left(_cards))
