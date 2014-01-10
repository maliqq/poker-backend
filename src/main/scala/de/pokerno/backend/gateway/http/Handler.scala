package de.pokerno.backend.gateway.http

import io.netty.buffer
import io.netty.util.CharsetUtil
import io.netty.channel.{ChannelFuture, ChannelFutureListener}
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, SimpleChannelInboundHandler}
import io.netty.handler.codec.http

import org.slf4j.LoggerFactory

trait DefaultHttpResponder {
  import http.HttpHeaders.isKeepAlive
  def includeCorsHeaders: Boolean = false
  
  def sendHttpResponse(ctx: ChannelHandlerContext, req: http.FullHttpRequest, resp: http.FullHttpResponse): ChannelFuture = {
    if (resp.getStatus.code == 200) {
      if (includeCorsHeaders) CorsHeadersResponder.sendHttpResponse(req, resp)
    } else {
      ErrorStatusResponder.sendHttpResponse(req, resp)
    }
    
    val f = ctx.channel.writeAndFlush(resp)
    if (!isKeepAlive(req) || resp.getStatus.code != 200)
      f.addListener(ChannelFutureListener.CLOSE)
    f
  }
}

trait HttpHandler extends DefaultHttpResponder {
  def handleHttpRequest(ctx: ChannelHandlerContext, req: http.FullHttpRequest)
}

object HttpHandler {
  def responseStatus(status: http.HttpResponseStatus) = new http.DefaultFullHttpResponse(http.HttpVersion.HTTP_1_1, status)
  def ok = responseStatus(http.HttpResponseStatus.OK)
  def badRequest = responseStatus(http.HttpResponseStatus.BAD_REQUEST)
  def notFound = responseStatus(http.HttpResponseStatus.NOT_FOUND)
  def forbidden = responseStatus(http.HttpResponseStatus.FORBIDDEN)
}

import scala.util.matching.Regex

class PathHandler(path: String, handler: HttpHandler) extends ChannelInboundHandlerAdapter {
  
  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = msg match {
    case req: http.FullHttpRequest =>
      val q = new http.QueryStringDecoder(req.getUri)
      if (q.path != path) {
        ctx.fireChannelRead(req)
        return
      }
      handler.handleHttpRequest(ctx, req)
    
    case _ => ctx.fireChannelRead(msg)
  }
  
}

trait HttpResponder {
  def sendHttpResponse(req: http.FullHttpRequest, resp: http.FullHttpResponse)
}

object CorsHeadersResponder extends HttpResponder {
  import http.HttpHeaders.{Names, Values}
  import HttpHandler.ok
  
  def sendHttpResponse(req: http.FullHttpRequest, resp: http.FullHttpResponse) {
    resp.headers().add(Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
    resp.headers().add(Names.ACCESS_CONTROL_ALLOW_HEADERS, "*")
  }
}

object ErrorStatusResponder extends HttpResponder {
  import http.HttpHeaders.{setContentLength, Names}
  
  def sendHttpResponse(req: http.FullHttpRequest, resp: http.FullHttpResponse) {
    val buf = buffer.Unpooled.copiedBuffer(resp.getStatus.toString.toCharArray, CharsetUtil.UTF_8)
    resp.content.writeBytes(buf)
    buf.release
    resp.headers().add(Names.CONTENT_TYPE, "text/plain")
    setContentLength(resp, resp.content.readableBytes)
  }
}
