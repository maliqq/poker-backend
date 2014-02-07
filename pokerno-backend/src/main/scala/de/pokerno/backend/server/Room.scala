package de.pokerno.backend.server

import akka.actor.Actor

object Room {
  case class Start(id: String)
  case class Stop(id: String)
  case class Send(id: String, msg: Any)
}

class Room extends Actor {
  override def preStart {

  }

  def receive = {
    case _ ⇒
  }

  override def postStop {

  }
}
