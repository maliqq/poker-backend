package de.pokerno.backend.server

import akka.actor.Actor
import de.pokerno.model

object Room {
  case class Start(params: Params)
  case class Send(id: String, msg: Any)
  case class Stop(id: String)
  
  case class Params(id: String, variation: model.Variation, stake: model.Stake)
}

class Room(params: Room.Params) extends Actor {
  override def preStart {

  }

  def receive = {
    case _ ⇒
  }

  override def postStop {

  }
}
