package de.pokerno.protocol.api

import de.pokerno.model._

import com.fasterxml.jackson.annotation.{JsonProperty, JsonGetter, JsonIgnore, JsonUnwrapped, JsonInclude}
import de.pokerno.gameplay
import de.pokerno.model

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class PlayState(
    ctx: gameplay.Context
  ) {
  @JsonUnwrapped val play = ctx.play
  @JsonUnwrapped val round = ctx.round
}

trait Round {
  def ctx: gameplay.Context
  @JsonUnwrapped val round = ctx.round
}

trait Button {
  def ctx: gameplay.Context
  @JsonGetter("button") def button = ctx.table.button
}

trait Seating {
  def play: model.Play
  @JsonGetter("seating") def seating = play.seating
  @JsonGetter("stacks") def stacks = play.stacks
}

// winners and losers
trait Winners {
  def play: model.Play
  @JsonGetter("net") def net = play.net
}

trait Deck {
  def play: model.Play
  @JsonGetter("deck") def deck = play.dealer.deck
}

// actions
trait Actions {
  def play: model.Play
  @JsonGetter("actions") def actions = play.actions
}

class PlayWrapper(_play: model.Play) {
  @JsonUnwrapped val play = _play
}
