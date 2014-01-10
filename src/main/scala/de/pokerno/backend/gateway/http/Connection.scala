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
  def write(msg: Any) = {
    val writing = channel.writeAndFlush(msg)
    writing.addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
    writing
  }
}

trait ChannelConnections[T <: Connection] {
  def gw: ActorRef
  def newConnection(channel: Channel, req: http.FullHttpRequest): T
  val channelConnections = new java.util.concurrent.ConcurrentHashMap[Channel, T]()
  
  def connect(channel: Channel, req: http.FullHttpRequest) = {
    val conn = newConnection(channel, req)
    channelConnections.put(channel, conn)
    gw ! Gateway.Connect(conn)
  }
  
  def disconnect(channel: Channel): T = {
    val conn = channelConnections.remove(channel)
    if (conn != null) gw ! Gateway.Disconnect(conn)
    conn
  }
  
  def message(channel: Channel, data: String) = {
    val conn = channelConnections.get(channel)
    if (conn != null) gw ! Gateway.Message(conn, data)
  }
}
