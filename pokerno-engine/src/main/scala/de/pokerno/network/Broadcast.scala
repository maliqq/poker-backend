package de.pokerno.network

trait Codec {
  def encode[T](msg: T): String
}

trait Broadcast[T <: Codec, M] {
  
  def fanout(msg: M)(implicit connections: Iterable[PlayerConnection], codec: T) {
    val data = codec.encode(msg)
    connections.map { _.send(data) }
  }
  
  def direct(msg: M, to: String)(implicit connections: Iterable[PlayerConnection], codec: T) {
    val data = codec.encode(msg)
    connections.find { _.sessionId == to } map { _.send(data) }
  }
  
}
