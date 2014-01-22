package de.pokerno.backend.gateway.stomp

import io.netty.channel.{SimpleChannelInboundHandler, ChannelHandlerContext}

import asia.stampy.common.message.StampyMessage

class Handler extends SimpleChannelInboundHandler[StampyMessage[_]] {
  
  override def channelRead0(ctx: ChannelHandlerContext, msg: StampyMessage[_]) {
  }
  
}
