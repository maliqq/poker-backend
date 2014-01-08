package de.pokerno.backend.gateway.http

import io.netty.channel.{ChannelHandlerContext, ChannelFutureListener}
import io.netty.handler.codec.http.HttpRequest

class Connection(
    ctx: ChannelHandlerContext,
    request: HttpRequest,
    executor: java.util.concurrent.Executor
) {
  
  def write(msg: Any) = {
    val writing = ctx.channel.write(msg)
    writing.addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
    writing
  }
  
  def close {
    ctx.channel.close
  }
}

class WebSocketConnection(
    ctx: ChannelHandlerContext,
    request: HttpRequest,
    executor: java.util.concurrent.Executor
) extends Connection(ctx, request, executor) {
}
