package de.pokerno.backend.gateway.stomp

import io.netty.channel._
import io.netty.util.CharsetUtil
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.{DelimiterBasedFrameDecoder, Delimiters}
import asia.stampy.common.parsing.StompMessageParser

class Decoder extends DelimiterBasedFrameDecoder(Int.MaxValue, false, true, Delimiters.nulDelimiter:_*) {
  val parser = new StompMessageParser
  
  override def decode(ctx: ChannelHandlerContext, buf: ByteBuf): Object = {
    parser.parseMessage(buf.toString(CharsetUtil.UTF_8))
  }
}
