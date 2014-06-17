package de.pokerno.backend.server.hub

trait Topic extends Exchange with Consumer {

  def name: String

  override def consume(msg: Message) = {
    consumers.map(_.consume(msg))
  }

}
