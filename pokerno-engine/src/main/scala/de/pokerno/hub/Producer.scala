package de.pokerno.hub

trait Producer[T, E <: Exchange[T]] {
  def exchange: E
  
  def publish(msg: T) {
    exchange.publish(msg)
  }
}
