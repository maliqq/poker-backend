package de.pokerno.backend.gateway.http

import akka.actor.ActorRef

import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{websocketx => ws}
import io.netty.handler.codec.http
import io.netty.channel.{Channel, ChannelFuture, ChannelFutureListener}

object WebSocket {
  class Connection(
      channel: Channel,
      request: http.FullHttpRequest) extends HttpConnection(channel, request) {
    
    override def write(data: Any) = data match {
      case f: ws.TextWebSocketFrame => super.write(f)
      case s: String => super.write(new ws.TextWebSocketFrame(s))
      //case b: ByteBuf => super.write(new ws.BinaryWebSocketFrame(b))
      case x => super.write(x) // FIXME handle this
    }
    
  }
  
  final val defaultPath = "/_ws"
    
  case class Config(val path: String = defaultPath)
  
  object Handler {
    final val Name = "http-websocket-handler"
  }

  class Handler(val path: String, val gw: ActorRef)
      extends SimpleChannelInboundHandler[Object] with HttpHandler with ChannelConnections[Connection] {
    import HttpHandler._
    import http.HttpHeaders._
    
    override def handlerAdded(ctx: ChannelHandlerContext) {
      ctx.pipeline.addBefore(Handler.Name, "path-handler-websocket", new PathHandler(path, this))
    }
    
    private def handshakerFactory(location: String) = new ws.WebSocketServerHandshakerFactory(location, null, false)
    
    def handshaker(req: http.FullHttpRequest) = {
      val h = handshakerFactory(webSocketLocation(req)).newHandshaker(req)
      if (h != null) Some(h) else None
    }
    
    private var _handshaker: Option[ws.WebSocketServerHandshaker] = None 
    
    override def channelRead0(ctx: ChannelHandlerContext, msg: Object): Unit = msg match {
      case frame: ws.WebSocketFrame => handleWebSocketFrame(ctx, frame)
      case req: http.FullHttpRequest => handleHttpRequest(ctx, req)
    }
    
    override def channelInactive(ctx: ChannelHandlerContext) {
      disconnect(ctx.channel)
    }
    
    def connection(channel: Channel, req: http.FullHttpRequest) = new Connection(channel, req)
    
    def handleWebSocketFrame(ctx: ChannelHandlerContext, frame: ws.WebSocketFrame): Unit = frame match {
      case close: ws.CloseWebSocketFrame =>
        _handshaker.foreach(_.close(ctx.channel, frame.retain.asInstanceOf[ws.CloseWebSocketFrame]))
      
      case ping: ws.PingWebSocketFrame =>
        ctx.channel.write(new ws.PongWebSocketFrame(frame.content.retain))
      
      case txt: ws.TextWebSocketFrame =>
        message(ctx.channel, txt.text)
      
      case _ =>
        //
    }
    
    def handleHttpRequest(ctx: ChannelHandlerContext, req: http.FullHttpRequest) {
      if (!req.getDecoderResult.isSuccess) {
        sendHttpResponse(ctx, req, badRequest)
        return
      }
      
      if (req.getMethod != http.HttpMethod.GET) {
        sendHttpResponse(ctx, req, forbidden)
        return
      }
      
      _handshaker = handshaker(req)
      if (_handshaker.isEmpty) {
        ws.WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel)
        return
      }
      
      val f = _handshaker.get.handshake(ctx.channel, req)
      f.addListener(new ChannelFutureListener {
        override def operationComplete(future: ChannelFuture) = connect(ctx.channel, req)
      })
    }
    
    def webSocketLocation(req: http.FullHttpRequest): String =
      "ws://" + req.headers().get(Names.HOST) + path
    
    override def channelReadComplete(ctx: ChannelHandlerContext) = ctx.flush
  }
}
