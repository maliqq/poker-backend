package de.pokerno.protocol.action

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import de.pokerno.model.{Bet, BetLike}

class AddBetConverter extends com.fasterxml.jackson.databind.util.StdConverter[BetLike, AddBet] {
  def convert(b: BetLike): AddBet = AddBet(b.bet)
}

@JsonDeserialize(converter = classOf[AddBetConverter])
sealed case class AddBet(
  bet: Bet
) extends PlayerEvent {}
