package de.pokerno.backend.handler

import akka.actor.Actor
import de.pokerno.gameplay.Notification
import de.pokerno.protocol.msg
import de.pokerno.form.Room
import de.pokerno.backend.server.Node
import de.pokerno.client.sync.Client

/*
Room, Table, Player state sync
*/

class SyncHandler(client: Client) extends Actor {
  def receive = {
    case Node.Metrics(id, metrics) =>
      client.reportNodeMetrics(id, metrics)

    case Room.Metrics.PlayStatsUpdate(id, metrics) =>
      client.reportRoomMetrics(id, metrics)

    case Room.ChangedState(id, newState) =>
      client.changeRoomState(id, newState.toString())

    case Notification(payload, roomId, _, _) =>
      payload match {
        case msg.PlayerJoin(pos, amount) =>
          client.startSession(roomId, pos.player, pos.pos, amount.toDouble)

        case msg.PlayerLeave(pos, _) => // TODO tell how much money left
          client.stopSession(roomId, pos.player, pos.pos, 0) // FIXME amount?

        case _ => // ignore
      }

    case _ => // ignore
  }
}
