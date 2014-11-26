package de.pokerno.network

trait Codec {
  def encode[T](msg: T): String
}

trait Broadcast[T <: Codec, P <: PlayerConnection] {
  def direct(msg: Any)(implicit connections: Iterable[P], codec: T) {
    val data = codec.encode(msg)
    connections.map { _.send(data) }
  }
  
  def direct(msg: Any, to: String)(implicit connections: Iterable[P], codec: T) {
    val data = codec.encode(msg)
    connections.find { _.sessionId == to } map { _.send(data) }
  }
  
}
