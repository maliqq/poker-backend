package de.pokerno.backend.gateway.http

import io.netty.buffer.{ByteBuf, DefaultByteBufHolder}
import io.netty.channel.{Channel, ChannelHandler, ChannelHandlerContext}
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.http.{HttpResponse, HttpResponseStatus, HttpHeaders}

object EventSource {
  final val handshakeHeaders = Map[String, String](
      HttpHeaders.Names.CONTENT_TYPE ->       "text/event-stream",
      HttpHeaders.Names.TRANSFER_ENCODING ->  "identity",
      HttpHeaders.Names.CONNECTION ->         "keep-alive",
      HttpHeaders.Names.CACHE_CONTROL ->      "no-cache"    
  )
  
  class Handshake {
    def handle(resp: HttpResponse, ctx: ChannelHandlerContext, handler: ChannelHandler) {
      resp.setStatus(HttpResponseStatus.OK)
      handshakeHeaders.map { case (header, value) =>
        resp.headers().add(header, value)
      }
      ctx.channel.write(resp)
      ctx.channel.pipeline.addLast(handler)
    }
  }
  
  case class Packet(val data: String,
      val comment: Boolean = false,
      val event: String = null,
      val id: java.lang.Long = null) {
    
    private object Token {
      final val Id = "id: "
      final val Event = "event: "
      final val Comment = ": "
      final val Data = "data: "
    }
    
    override def toString = {
      var b = new StringBuffer
      var header = Token.Data
      
      if (comment)
        header = Token.Comment
      else {
        if (id != null)
          b.append(Token.Id).append(id.toString)
        if (event != null)
          b.append(Token.Event).append(event.replaceAll("\n", "")).append("\n")
      }

      data.split("\n").foreach { line =>
        b.append(header).append(line).append("\n")
      }
      
      b.append("\n").toString
    }
  }
  
  class Encoder extends MessageToByteEncoder[Packet] {
    def encode(ctx: ChannelHandlerContext, packet: Packet, out: ByteBuf) = {
      
    }
  }
}
