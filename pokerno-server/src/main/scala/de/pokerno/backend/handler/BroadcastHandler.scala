package de.pokerno.backend.handler

import com.fasterxml.jackson.databind.ObjectMapper
import akka.actor.Actor
import de.pokerno.form.Room
import de.pokerno.backend.broadcast.Redis

class BroadcastHandler extends Actor {
  final val channel = "room.state"
  final val mapper = new ObjectMapper()
  private val bcast = Redis()

  def receive = {
    case Room.Created(id) =>
      bcast.broadcast(channel,
        """{"type":"created","id":"%s","payload":{"players_count":0}}""".format(id))

    case Room.ChangedState(id, newState) =>
      bcast.broadcast(channel,
        """{"type":"updated","id":"%s","payload":{"state":"%s"}}""".format(id, newState))

    case Room.Metrics.PlayStatsUpdate(id, metrics) =>
      bcast.broadcast(channel,
        """{"type":"updated","id":"%s","payload":%s}""".format(id, mapper.writeValueAsString(metrics)))

    case Room.Metrics.PlayersCountUpdate(id, metrics) =>
      bcast.broadcast(channel,
        """{"type":"updated","id":"%s","payload":{"players_count": %d}}""".format(id, metrics.playersCount))

    case _ =>
  }
}
