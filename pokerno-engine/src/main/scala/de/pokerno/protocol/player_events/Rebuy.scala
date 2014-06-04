package de.pokerno.protocol.player_events

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class Rebuy() extends PlayerEvent {}
