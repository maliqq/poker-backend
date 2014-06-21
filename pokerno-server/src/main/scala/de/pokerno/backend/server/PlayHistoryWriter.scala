package de.pokerno.backend.server

import akka.actor.{Actor, ActorLogging}
import math.{BigDecimal => Decimal}

import de.pokerno.model
import de.pokerno.backend.Storage

class PlayHistoryWriter(val storage: Storage) extends Actor with ActorLogging {

  override def preStart {
  }
  
  def receive = {
    case (id: java.util.UUID, game: model.Game, stake: model.Stake, play: model.Play) =>
      log.info("writing {} {}", id, play)
      storage.write(id, game, stake, play)
  }

}

