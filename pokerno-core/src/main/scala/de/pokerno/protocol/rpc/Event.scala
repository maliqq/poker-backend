package de.pokerno.protocol.rpc

case class PlayerEvent(`type`: PlayerEventSchema.EventType, player: String) {
  def this() = this(null, null)
}

case class StackEvent(`type`: StackEventSchema.EventType, player: String, amount: java.lang.Double) {
  def this() = this(null, null, null)
}
