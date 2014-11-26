package de.pokerno.hub

trait Producer[T] {
  def exchange: Exchange[T]
  
  def publish(msg: T) {
    exchange.publish(msg)
  }
}
