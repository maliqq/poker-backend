package de.pokerno.backend.gateway.http

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

class WebSocketHandler extends SimpleChannelInboundHandler[TextWebSocketFrame] {
  override def channelRead0(ctx: ChannelHandlerContext, frame: TextWebSocketFrame) {
    
  }
}
