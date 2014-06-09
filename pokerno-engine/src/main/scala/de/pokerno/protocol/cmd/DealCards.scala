package de.pokerno.protocol.cmd

import com.fasterxml.jackson.annotation.JsonProperty
import de.pokerno.model.DealType

sealed case class DealCards(
  @JsonProperty `type`: DealType.Value,
  @JsonProperty cards: Either[Cards, Option[Int]] = Right(None),
  @JsonProperty player: Option[Player] = None
) extends Command {}
