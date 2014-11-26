package de.pokerno.hub

trait Producer {
  def exchange: Exchange
  
  def publish(msg: Message) {
    exchange.publish(msg)
  }
}
