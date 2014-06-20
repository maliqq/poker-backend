package de.pokerno.backend

import akka.actor.{Actor, ActorLogging}

import de.pokerno.model.Play
import de.pokerno.backend.storage.Cassandra

class Storage extends Actor with ActorLogging {
  val client = new Cassandra.Client("localhost", "poker")
  
  override def preStart {
    log.info("starting cassandra client")
  }
  
  def receive = {
    case (id: java.util.UUID, play: Play) =>
      log.info("writing {} {}", id, play)
      client.write(id, play)
  }

}
