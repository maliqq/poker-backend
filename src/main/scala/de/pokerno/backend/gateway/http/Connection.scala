package de.pokerno.backend.gateway.http

import akka.actor.ActorRef

import io.netty.channel.{Channel, ChannelFuture, ChannelHandlerContext, ChannelFutureListener}
import io.netty.handler.codec.http

trait Connection {
  def remoteAddr: String
  def write(msg: Any): ChannelFuture
}

class HttpConnection(
    channel: Channel,
    req: http.FullHttpRequest) extends Connection {
  
  def remoteAddr = channel.remoteAddress.toString
  
  def write(msg: Any) = channel.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
}

trait ChannelConnections[T <: Connection] {
  def gw: ActorRef
  def connection(channel: Channel, req: http.FullHttpRequest): T
  
  import collection.JavaConverters._
  val channelConnections = new java.util.concurrent.ConcurrentHashMap[Channel, T]().asScala
  
  def connect(channel: Channel, req: http.FullHttpRequest) = {
    val conn = connection(channel, req)
    if (channelConnections.putIfAbsent(channel, conn).isEmpty)
      gw ! Gateway.Connect(conn)
  }
  
  def disconnect(channel: Channel) = {
    val conn = channelConnections.remove(channel)
    conn.foreach(gw ! Gateway.Disconnect(_))
    conn
  }
  
  def broadcast(msg: Any) = channelConnections.map { case (channel, conn) =>
    conn.write(msg)
  }
  
  def message(channel: Channel, data: String) =
    channelConnections.get(channel).foreach(gw ! Gateway.Message(_, data))
}
