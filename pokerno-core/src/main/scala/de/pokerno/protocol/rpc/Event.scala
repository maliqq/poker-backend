package de.pokerno.protocol.rpc

import beans._
import de.pokerno.protocol.{ Message ⇒ BaseMessage }

case class PlayerEvent(
    @BeanProperty
    `type`: PlayerEventSchema.EventType,
    @BeanProperty
    player: String) extends BaseMessage {
  def schema = PlayerEventSchema.SCHEMA
  def this() = this(null, null)
}

case class StackEvent(
    @BeanProperty
    `type`: StackEventSchema.EventType,
    @BeanProperty
    player: String,
    @BeanProperty
    amount: java.lang.Double) extends BaseMessage {
  def schema = StackEventSchema.SCHEMA
  def this() = this(null, null, null)
}
