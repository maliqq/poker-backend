package de.pokerno.client.history

import math.{BigDecimal => Decimal}
import de.pokerno.{model, poker}
import de.pokerno.protocol.api

class Client(val baseUrl: String) extends de.pokerno.client.HttpClient {
  def write(roomId: java.util.UUID, game: model.Game, stake: model.Stake, play: model.Play) {
    // TODO: broadcast to kafka
    val playState = new api.PlayWrapper(play)
      with api.Seating
      with api.Winners
      //with api.Button
      with api.Deck
      with api.Actions

    path("/api/v1/plays").
      data(playState).
      params(
        Map("room_id" -> roomId.toString())
      ).
      post()
  }
}
