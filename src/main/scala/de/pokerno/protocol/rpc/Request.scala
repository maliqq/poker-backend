package de.pokerno.protocol.rpc

import de.pokerno.protocol.wire
import de.pokerno.protocol.{Message => BaseMessage}

import beans._
import org.msgpack.annotation.{ Message => MsgPack }
import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonSubTypes}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "$method"
)
@JsonSubTypes(Array(
  // create new room
  new JsonSubTypes.Type(value = classOf[CreateRoom], name="CreateRoom")
))
abstract class Request extends BaseMessage {
}

@MsgPack
sealed case class CreateRoom(
    @BeanProperty
    var id: String,
    @BeanProperty
    var table: wire.Table,
    @BeanProperty
    var variation: wire.Variation,
    @BeanProperty
    var stake: wire.Stake
) {
  def this() = this(null, null, null, null)
}

@MsgPack
sealed case class KickPlayer(
    @BeanProperty
    var player: String,
    @BeanProperty
    var reason: String
) {
  def this() = this(null, null)
}
