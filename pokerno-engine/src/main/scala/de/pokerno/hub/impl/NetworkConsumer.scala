package de.pokerno.hub.impl

class NetworkConsumer[T](conn: de.pokerno.network.NetworkConnection) extends de.pokerno.hub.Consumer[T] {
  def consume(msg: T) {
    conn.send(msg)
  }
}
