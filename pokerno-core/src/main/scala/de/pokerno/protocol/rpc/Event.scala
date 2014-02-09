package de.pokerno.protocol.rpc

import beans._
import de.pokerno.protocol.{ Message ⇒ BaseMessage }

case class PlayerEvent(
    @BeanProperty
    var `type`: PlayerEventSchema.EventType,
    @BeanProperty
    var player: String) extends BaseMessage {
  def schema = PlayerEventSchema.SCHEMA
  def this() = this(null, null)
}

case class StackEvent(
    @BeanProperty
    var `type`: StackEventSchema.EventType,
    @BeanProperty
    var player: String,
    @BeanProperty
    var amount: java.lang.Double) extends BaseMessage {
  def schema = StackEventSchema.SCHEMA
  def this() = this(null, null, null)
}
