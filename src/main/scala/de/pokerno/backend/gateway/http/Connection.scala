package de.pokerno.backend.gateway.http

import akka.actor.ActorRef

import io.netty.channel.{Channel, ChannelFuture, ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelFutureListener}
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
  
  def connection(ch: Channel, req: http.FullHttpRequest): HttpConnection
  
  def connect(channel: Channel, req: http.FullHttpRequest) {
    gw ! Gateway.Connect(channel, connection(channel, req))
  }
  
  def disconnect(channel: Channel) {
    gw ! Gateway.Disconnect(channel)
  }
  
  def message(channel: Channel, data: String) {
    gw ! Gateway.Message(channel, data)
  }
}
