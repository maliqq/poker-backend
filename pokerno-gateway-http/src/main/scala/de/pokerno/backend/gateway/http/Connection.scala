package de.pokerno.backend.gateway.http

import akka.actor.ActorRef

import io.netty.buffer.Unpooled
import io.netty.channel.{ Channel, ChannelFutureListener }
import io.netty.handler.codec.http

class HttpConnection(
    channel: Channel,
    req: http.FullHttpRequest) extends Connection {

  def remoteAddr = channel.remoteAddress.toString
  
  val q = new http.QueryStringDecoder(req.getUri())
  
  private def param(key: String): Option[String] = {
    val v = q.parameters().get(key)
    if (v != null && v.size() > 0)
      Some(v.get(0))
    else None
  }
  
  private val _sessionId = java.util.UUID.randomUUID()
  def sessionId: String = _sessionId.toString()
  
  final val authKey = "auth"
  def auth = param(authKey)
  
  final val roomKey = "room"
  def room = param(roomKey)

  // FIXME
  def player = auth

  def write(msg: Any) = {
    if (channel.isActive)
      channel.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }
  
  def close() {
    if (channel.isActive())
      channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
  }

  def send(msg: Any) = write(msg)

  override def toString = f"room:$room session:$sessionId player:$player"
}

object ConnectionEvent {
  case class Connect(channel: Channel, conn: Connection)
  case class Disconnect(channel: Channel)
  case class Message(channel: Channel, data: String)
}

trait ChannelConnections[T <: Connection] {

  def gw: ActorRef

  def connection(ch: Channel, req: http.FullHttpRequest): HttpConnection

  def connect(channel: Channel, req: http.FullHttpRequest) {
    gw ! ConnectionEvent.Connect(channel, connection(channel, req))
  }

  def disconnect(channel: Channel) {
    gw ! ConnectionEvent.Disconnect(channel)
  }

  def message(channel: Channel, data: String) {
    gw ! ConnectionEvent.Message(channel, data)
  }

}
