package de.pokerno.backend.gateway.websocket

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

class Handler extends SimpleChannelInboundHandler[TextWebSocketFrame] {
  override def channelRead0(ctx: ChannelHandlerContext, frame: TextWebSocketFrame) {
    
  }
}
