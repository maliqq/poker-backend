package de.pokerno.protocol.action

import com.fasterxml.jackson.annotation.JsonProperty

sealed case class BuyIn() extends PlayerEvent {}
