package de.pokerno.backend.gateway.http

import akka.actor.ActorRef
import io.netty.buffer.{ByteBuf, Unpooled, DefaultByteBufHolder}
import io.netty.util.CharsetUtil
import io.netty.channel._
import io.netty.handler.codec.{MessageToByteEncoder, MessageToMessageEncoder}
import io.netty.handler.codec.http

object EventSource {
  import http.HttpHeaders.{Names, Values}
  
  final val defaultPath = "/_events"
    
  case class Config(var path: String = defaultPath)
  
  class Connection(
      channel: Channel,
      request: http.FullHttpRequest) extends HttpConnection(channel, request) {
    
    override def send(data: Any) = data match {
      case p: Packet =>
        write(p)

      case s: String =>
        sendString(s)
        
      case bytes: Array[Byte] =>
        sendBytes(bytes)
      
      case x =>
        // FIXME: logger.warn("Unknown message type: %s" format(x))
    }

    def sendBytes(bytes: Array[Byte]) = {
      val buf = Unpooled.copiedBuffer(bytes)
      val s = buf.toString(CharsetUtil.UTF_8)
      sendString(s)
    }

    def sendString(s: String) =
      write(new Packet(s))
  }
  
  private object Handshaker extends DefaultHttpResponder {
    import HttpHandler.ok
    
    override val includeCorsHeaders = true
    
    object Mimes {
      final val EventStream = "text/event-stream" 
    }
    
    def handshake(ctx: ChannelHandlerContext, req: http.FullHttpRequest) = {
      val promise = ctx.channel.newPromise
      val p = ctx.channel.pipeline
      val encoderCtx = p.context(classOf[http.HttpResponseEncoder])
      p.addBefore(encoderCtx.name, "eventsource-encoder", new Encoder)
      
      val resp = ok
      resp.headers().add(Names.CONTENT_TYPE, Mimes.EventStream)
      resp.headers().add(Names.TRANSFER_ENCODING, Values.IDENTITY)
      resp.headers().add(Names.CONNECTION, Values.KEEP_ALIVE)
      resp.headers().add(Names.CACHE_CONTROL, Values.NO_CACHE)
      
      sendHttpResponse(ctx, req, resp).addListener(new ChannelFutureListener {
        override def operationComplete(f: ChannelFuture) {
          if (f.isSuccess) promise.setSuccess()
          else promise.setFailure(f.cause)
        }
      })
      
      promise
    }
  }
  
  case class Packet(data: String,
      id: Option[String] = None,
      event: Option[String] = None,
      comment: Boolean = false) {
    
    private object Token {
      final val Id = "id: "
      final val Event = "event: "
      final val Comment = ": "
      final val Data = "data: "
      final val Lf = "\n"
    }
    
    lazy val header = if (comment) Token.Comment else Token.Data
    
    def build = {
      val b = new StringBuilder
      
      if (!comment) {
        id.foreach(b.append(Token.Id).append(_))
        event.map(_.replaceAll(Token.Lf, "")).foreach(b.append(Token.Event).append(_).append(Token.Lf))
      }
      
      for(line <- data.split("(\r?\n)|\r")) {
        b.append(header).append(line).append(Token.Lf)
      }
      
      b.append(Token.Lf).toString()
    }
  }
  
  object Handler {
    final val Name = "http-eventsource-handler"
  }
  
  class Handler(val path: String, val gw: ActorRef) extends ChannelInboundHandlerAdapter with HttpHandler with ChannelConnections[Connection] {
    import HttpHandler._
    
    override def handlerAdded(ctx: ChannelHandlerContext) {
      ctx.pipeline.addBefore(Handler.Name, "path-handler-eventsource", new PathHandler(path, this))
    }
    
    override def channelActive(ctx: ChannelHandlerContext) {
    } 
    
    override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = msg match {
      case req: http.FullHttpRequest =>
        handleHttpRequest(ctx, req)
      
      case _ => ctx.fireChannelRead(msg)
    }
    
    def connection(channel: Channel, req: http.FullHttpRequest) = new Connection(channel, req)
    
    def handleHttpRequest(ctx: ChannelHandlerContext, req: http.FullHttpRequest) {
      if (req.getMethod != http.HttpMethod.GET) {
        sendHttpResponse(ctx, req, forbidden)
        return
      }
      
      val handshaking = Handshaker.handshake(ctx, req)
      handshaking.addListener(new ChannelFutureListener{
        override def operationComplete(f: ChannelFuture) {
          if (!f.isSuccess) ctx.fireExceptionCaught(f.cause)
          else connect(ctx.channel, req)
        }
      })
    }
    
    override def channelInactive(ctx: ChannelHandlerContext) =
      disconnect(ctx.channel)
  }
  
  class Encoder extends MessageToMessageEncoder[Packet] { // FIXME
    import io.netty.buffer.Unpooled._
    
    override def encode(ctx: ChannelHandlerContext, packet: Packet, out: java.util.List[Object]) =
      out.add(copiedBuffer(packet.build, CharsetUtil.UTF_8))
  }
  
}
