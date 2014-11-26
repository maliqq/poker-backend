package de.pokerno.network

trait Codec {
  def encode[T](msg: T): String
}

trait Broadcast[T <: Codec] {
  def broadcast(msg: Any)(implicit connections: Iterable[PlayerConnection], codec: T) {
    val data = codec.encode(msg)
    connections.map { _.send(data) }
  }
  
  def broadcast(msg: Any, to: String)(implicit connections: Iterable[PlayerConnection], codec: T) {
    val data = codec.encode(msg)
    connections.find { _.sessionId == to } map { _.send(data) }
  }
  
}