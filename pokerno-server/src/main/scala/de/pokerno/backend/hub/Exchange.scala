package de.pokerno.backend.server.hub

trait Exchange {

  val consumers = collection.mutable.ListBuffer[Consumer]()

  def register(consumer: Consumer) {
    consumers += consumer
  }

  def unregister(consumer: Consumer) {
    consumers -= consumer
  }

}
