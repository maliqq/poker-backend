package de.pokerno.backend

import de.pokerno.protocol
import akka.actor.ActorRef

object Gateway {
  case class Message(self: ActorRef, msg: Any)
}
