package de.pokerno.replay

import akka.actor.ActorRef

import io.netty.buffer.Unpooled
import io.netty.channel.{Channel, SimpleChannelInboundHandler, ChannelHandlerContext, ChannelFutureListener}
import io.netty.handler.codec.http
import io.netty.handler.codec.http.{FullHttpRequest, QueryStringDecoder}
import io.netty.util.CharsetUtil
import com.fasterxml.jackson.databind.ObjectMapper

import concurrent.duration._

private[replay] class ApiHandler(gw: ActorRef) extends SimpleChannelInboundHandler[FullHttpRequest] {

  import http.HttpHeaders._
  import http.HttpMethod
  
  lazy val mapper = new ObjectMapper
  
  override def channelRead0(ctx: ChannelHandlerContext, req: FullHttpRequest) {
    val q = new QueryStringDecoder(req.getUri)
    req.retain()
    
    if (q.path == "/_api/scenario") {
      val resp = new http.DefaultFullHttpResponse(http.HttpVersion.HTTP_1_1, http.HttpResponseStatus.OK)
      resp.headers().add(Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
      resp.headers().add(Names.ACCESS_CONTROL_ALLOW_HEADERS, "*")
      
      req.getMethod() match {
        case HttpMethod.HEAD =>
          sendResp(ctx, resp)
        case HttpMethod.POST =>
          val content = req.content().toString(CharsetUtil.UTF_8)
          gw ! (content, ctx, resp)
        case _ =>
          resp.setStatus(http.HttpResponseStatus.METHOD_NOT_ALLOWED)
          sendResp(ctx, resp)
      }
      
    } else {
      val resp = new http.DefaultFullHttpResponse(http.HttpVersion.HTTP_1_1, http.HttpResponseStatus.NOT_FOUND)
      sendResp(ctx, resp)
    }
  }
  
  def sendResp(ctx: ChannelHandlerContext, resp: http.FullHttpResponse) {
    ctx.channel.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE)
  }

}
