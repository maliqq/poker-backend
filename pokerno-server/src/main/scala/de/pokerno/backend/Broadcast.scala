package de.pokerno.backend

object Broadcast {
  type Message = String
}

abstract class Broadcast {
  def broadcast(topic: String, msg: Broadcast.Message): Unit
}
