package de.pokerno.protocol.player_events

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class BuyIn() extends PlayerEvent {}
