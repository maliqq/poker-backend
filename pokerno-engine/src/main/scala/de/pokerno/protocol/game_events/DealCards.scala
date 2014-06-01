package de.pokerno.protocol.game_events

import de.pokerno.model.DealType

import beans._
import com.fasterxml.jackson.annotation.{JsonInclude, JsonIgnore}

@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class DealCards(
    _cards: Either[Cards, Int]
) extends GameEvent {
  
    @BeanProperty val cards: Cards = _cards match {
      case Left(c) => c
      case _ => Array()
    }

    @BeanProperty val cardsNum: Option[Int] = _cards match {
      case Right(n) => Some(n)
      case _ => None
    }
}

sealed case class DealHole(
    @BeanProperty pos: Int,
    @BeanProperty player: Player,
    @JsonIgnore _cards: Either[Cards, Int]
  ) extends DealCards(_cards)

sealed case class DealDoor(
    @BeanProperty pos: Int,
    @BeanProperty player: Player,
    @JsonIgnore _cards: Either[Cards, Int]    
  ) extends DealCards(_cards)

sealed case class DealBoard(
    @JsonIgnore _cards: Cards = Array()
) extends DealCards(Left(_cards))
