package de.pokerno.backend.gateway.http

import akka.actor.ActorRef
import io.netty.buffer.{ByteBuf, DefaultByteBufHolder}
import io.netty.channel.{
  Channel, ChannelHandler, ChannelHandlerContext,
  ChannelPromise, ChannelFuture, ChannelFutureListener,
  ChannelInboundHandlerAdapter, ChannelOutboundHandlerAdapter}
import io.netty.handler.codec.{MessageToByteEncoder, MessageToMessageEncoder}
import io.netty.handler.codec.http

object EventSource {
  import http.HttpHeaders.{Names, Values}
  
  class Connection(
      channel: Channel,
      request: http.FullHttpRequest) extends HttpConnection(channel, request) {
  }
  
  object Handshaker extends DefaultHttpResponder {
    import HttpHandler.ok
    
    override val includeCorsHeaders = true
    
    def handshake(ctx: ChannelHandlerContext, req: http.FullHttpRequest) = {
      val promise = ctx.channel.newPromise
      val p = ctx.channel.pipeline
      val encoderCtx = p.context(classOf[http.HttpResponseEncoder])
      p.addAfter(encoderCtx.name, "eventsource-encoder", new Encoder)
      
      val resp = ok
      resp.headers().add(Names.CONTENT_TYPE, "text/event-stream")
      resp.headers().add(Names.TRANSFER_ENCODING, Values.IDENTITY)
      resp.headers().add(Names.CONNECTION, Values.KEEP_ALIVE)
      resp.headers().add(Names.CACHE_CONTROL, Values.NO_CACHE)
      
      sendHttpResponse(ctx, req, resp).addListener(new ChannelFutureListener{
        override def operationComplete(f: ChannelFuture) {
          if (f.isSuccess)
            promise.setSuccess()
          else
            promise.setFailure(f.cause)
        }
      })
      
      promise
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
      final val Lf = "\n"
    }
    
    def build = {
      var b = new StringBuilder
      var header = Token.Data
      
      if (comment)
        header = Token.Comment
      else {
        if (id != null)
          b.append(Token.Id).append(id.toString)
        if (event != null)
          b.append(Token.Event).append(event.replaceAll(Token.Lf, "")).append(Token.Lf)
      }

      data.split(Token.Lf).foreach { line =>
        b.append(header).append(line).append(Token.Lf)
      }
      
      b.append(Token.Lf).toString
    }
    
    override def toString = build
  }
  
  class Handler(val gw: ActorRef) extends ChannelInboundHandlerAdapter with HttpHandler with ChannelConnections[Connection] {
    import HttpHandler._
    
    override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = msg match {
      case req: http.FullHttpRequest =>
        handleHttpRequest(ctx, req)
      
      case _ => ctx.fireChannelRead(msg)
    }
    
    def newConnection(channel: Channel, req: http.FullHttpRequest) = new Connection(channel, req)
    
    def handleHttpRequest(ctx: ChannelHandlerContext, req: http.FullHttpRequest) {
      if (req.getMethod != http.HttpMethod.GET) {
        sendHttpResponse(ctx, req, forbidden)
        return
      }
      
      val f = Handshaker.handshake(ctx, req)
      f.addListener(new ChannelFutureListener{
        override def operationComplete(f: ChannelFuture) {
          if (!f.isSuccess) ctx.fireExceptionCaught(f.cause)
          else connect(ctx.channel, req)
        }
      })
    }
    
    override def channelInactive(ctx: ChannelHandlerContext) {
      val conn = disconnect(ctx.channel)
      if (conn == null) super.channelInactive(ctx)
    }
  }
  
  class Encoder extends MessageToMessageEncoder[Packet] { // FIXME
    def encode(ctx: ChannelHandlerContext, packet: Packet, out: java.util.List[Object]) = {
      out.add(packet.build)
    }
  }
  
}
