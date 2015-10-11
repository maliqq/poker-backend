package de.pokerno.client.sync

import de.pokerno.form.Room
import de.pokerno.backend.node
import de.pokerno.form.cash.Metrics

class Client(val baseUrl: String) extends de.pokerno.client.HttpClient {
  def getRooms(id: String) = {
    path(f"/api/v1/rooms").
      params(Map("node_id" -> id)).
      get()
  }

  def reportNodeMetrics(id: String, metrics: node.Metrics) {
    path(f"/api/v1/nodes/$id/metrics").
      data(metrics).
      post()
  }

  def reportRoomMetrics(id: String, metrics: Metrics) {
    path(f"/api/v1/room/$id/metrics").
      data(metrics).
      post()
  }

  def changeRoomState(id: String, state: String) {
    path(f"/api/v1/room/$id").
      data(f"""{"room":{"state":"$state'}}""").
      put()
  }

  def startSession(roomId: String, playerId: String, pos: Int, amount: Double) {
    path(f"/api/v1/sessions/start").
      params(Map(
        "room_id" -> roomId,
        "player_id" -> playerId,
        "session" -> Map(
	        "pos" -> pos,
	        "amount" -> amount
	      )
      )).
      get()
  }

  def stopSession(roomId: String, playerId: String, pos: Int, amount: Double) {
    path(f"/api/v1/sessions/stop").
      params(Map(
        "room_id" -> roomId,
        "player_id" -> playerId,
        "session" -> Map(
	        "pos" -> pos,
	        "amount" -> amount
	      )
      )).
      delete()
  }
}
