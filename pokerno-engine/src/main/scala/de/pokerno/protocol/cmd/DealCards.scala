package de.pokerno.protocol.cmd

import de.pokerno.model.DealType

sealed case class DealCards(
  _type: DealType.Value,
  cards: Either[Cards, Option[Int]] = Right(None),
  player: Option[Player] = None
) extends Command {}
