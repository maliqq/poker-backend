package de.pokerno.backend.handler

import akka.actor.Actor
import de.pokerno.client.history.Client

class HistoryHandler(client: Client) extends Actor {
  import de.pokerno.gameplay

  def receive = {
    case gameplay.Deal.Dump(id, game, stake, play) =>
      //log.info("writing {} {}", id, play)
      client.write(id, game, stake, play)
  }
}
