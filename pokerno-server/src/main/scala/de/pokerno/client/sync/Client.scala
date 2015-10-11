package de.pokerno.client.sync

import de.pokerno.form.Room
import de.pokerno.backend.server.Node
import de.pokerno.form.cash.Metrics

class Client(val baseUrl: String) extends de.pokerno.client.HttpClient {
  def getRooms(id: String) = {
    path(f"/api/v1/rooms.json").
      params(Map("node_id" -> id)).
      get()
  }

  def reportNodeMetrics(id: String, metrics: Node.Metrics) {
    path(f"/api/v1/node/$id/metrics").
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
	      "session" -> Map(
	        "player_id" -> playerId,
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
	      "session" -> Map(
	        "player_id" -> playerId,
	        "pos" -> pos,
	        "amount" -> amount
	      )
      )).
      delete()
  }
}
