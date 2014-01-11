package de.pokerno.backend.gateway.http

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.CharsetUtil
import io.netty.channel.{ChannelFuture, ChannelFutureListener}
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, SimpleChannelInboundHandler}
import io.netty.handler.codec.http

import org.slf4j.LoggerFactory

trait DefaultHttpResponder {
  import http.HttpHeaders.isKeepAlive
  def includeCorsHeaders: Boolean = false
  
  def sendHttpResponse(ctx: ChannelHandlerContext, req: http.FullHttpRequest, resp: http.FullHttpResponse): ChannelFuture = {
    val response = Response(resp)
    
    if (resp.getStatus.code == 200) {
    
      if (includeCorsHeaders) response.withCorsHeaders
    
    } else response.error(resp.getStatus.code.toString)
    
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
      if (q.path != path) ctx.fireChannelRead(req)
      else handler.handleHttpRequest(ctx, req)
    
    case _ => ctx.fireChannelRead(msg)
  }
  
}

trait HttpResponder {
  def sendHttpResponse(req: http.FullHttpRequest, resp: http.FullHttpResponse)
}

case class Response(resp: http.FullHttpResponse) {
  import http.HttpHeaders.{setContentLength, Names, Values}
  
  def setContentType(contentType: String) {
    resp.headers.add(Names.CONTENT_TYPE, contentType)
  }
  
  def withCorsHeaders = {
    resp.headers().add(Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
    resp.headers().add(Names.ACCESS_CONTROL_ALLOW_HEADERS, "*")
    this
  }
  
  def withBody(body: ByteBuf): Response = {
    setContentLength(resp, resp.content.readableBytes)
    resp.content.writeBytes(body)
    body.release
    this
  }
  
  def withBody(body: String): Response = withBody(Unpooled.copiedBuffer(body, CharsetUtil.UTF_8))
  
  def error(message: String) {
    setContentType("text/plain")
    withBody(message)
  }
  
} 
