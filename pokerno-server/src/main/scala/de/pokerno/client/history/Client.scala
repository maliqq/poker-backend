package de.pokerno.client.history

import math.{BigDecimal => Decimal}
import de.pokerno.{model, poker}

class Client(val baseUrl: String) extends de.pokerno.client.HttpClient {
  case class FormData(
      game: model.Game,
      stake: model.Stake,
      play: model.Play
      )
  def write(roomId: java.util.UUID, game: model.Game, stake: model.Stake, play: model.Play) {
    // TODO: broadcast to kafka
    post("/api/v1/plays", Options(
        data = Left(Some(FormData(game, stake, play))),
        params = Map("room_id" -> roomId.toString())
        ))
  }
}
