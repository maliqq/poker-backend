package de.pokerno.backend.server.hub

trait Exchange {
  
  var consumers: java.util.ArrayList[Consumer] = null
  
  def register(consumer: Consumer) {
    consumers.add(consumer)
  }
  
  def unregister(consumer: Consumer) {
    consumers.remove(consumer)
  }
  
}
