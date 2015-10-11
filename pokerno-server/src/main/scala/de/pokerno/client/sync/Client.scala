package de.pokerno.client.sync

import de.pokerno.form.Room
import de.pokerno.backend.server.Node
import de.pokerno.form.cash.Metrics

class Client(val baseUrl: String) extends de.pokerno.client.HttpClient {
  def getRooms(id: String) = {
    get(f"/api/v1/rooms.json", Options(
      params = Map("node_id" -> id)
    ))
  }
  
  def reportNodeMetrics(id: String, metrics: Node.Metrics) {
    post(f"/api/v1/node/$id/metrics", Options(data = Left(Some(metrics))))
  }

  def reportRoomMetrics(id: String, metrics: Metrics) {
    post(f"/api/v1/room/$id/metrics", Options(data = Left(Some(metrics))))
  }

  def changeRoomState(id: String, state: String) {
    put(f"/api/v1/room/$id", Options(data = Right(f"""{"room":{"state":"$state'}}""")))
  }

  def startSession(roomId: String, playerId: String, pos: Int, amount: Double) {
    post(f"/api/v1/sessions/start", Options(
      params = Map(
        "room_id" -> roomId,
	      "session" -> Map(
	        "player_id" -> playerId,
	        "pos" -> pos,
	        "amount" -> amount
	      )
      )
    ))
  }

  def stopSession(roomId: String, playerId: String, pos: Int, amount: Double) {
    delete(f"/api/v1/sessions/stop", Options(
      params = Map(
        "room_id" -> roomId,
	      "session" -> Map(
	        "player_id" -> playerId,
	        "pos" -> pos,
	        "amount" -> amount
	      )
      )
    ))
  }
}
