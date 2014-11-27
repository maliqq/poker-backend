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

import de.pokerno.model.seat.impl
import de.pokerno.model.seat.impl.{Sitting, Position}

object DealHole {
  def apply(pos: Position, cards: Either[Cards, Int]): DealHole = new DealHole(pos, cards)
  def unapply(v: DealHole): Option[(Position, Either[Cards, Int])] = Some((
      v.position,
      v.cardsNum.map(Right(_)) getOrElse Left(v.cards)
    ))
}

sealed class DealHole(
    @JsonUnwrapped val position: Position,
    _cards: Either[Cards, Int]
  ) extends DealCards(_cards)

object DealDoor {
  def apply(pos: Position, cards: Either[Cards, Int]): DealDoor = new DealDoor(pos, cards)
  def unapply(v: DealDoor): Option[(Position, Either[Cards, Int])] = Some((
      v.position,
      v.cardsNum.map(Right(_)) getOrElse Left(v.cards)
    ))
}

sealed class DealDoor(
    @JsonUnwrapped val position: Position,
    _cards: Either[Cards, Int]
  ) extends DealCards(_cards)

sealed case class DealBoard(
    @JsonSerialize(converter = classOf[Cards2Binary]) cards: Cards
) extends GameEvent
