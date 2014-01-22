package de.pokerno.backend.server.hub

trait Consumer {

  def consume(msg: Message)

}
