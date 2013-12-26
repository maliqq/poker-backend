package de.pokerno.backend.gateway.eventsource

import io.netty.channel.{ChannelHandler, ChannelHandlerContext}
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.{HttpResponse, HttpResponseStatus, HttpHeaders}

object EventSource {
  final val handshakeHeaders = Map[String, String](
      HttpHeaders.Names.CONTENT_TYPE -> "text/event-stream",
      HttpHeaders.Names.TRANSFER_ENCODING -> "identity",
      HttpHeaders.Names.CONNECTION -> "keep-alive",
      HttpHeaders.Names.CACHE_CONTROL -> "no-cache"    
  )
  
  class Handshake {
    def handle(resp: HttpResponse, ctx: ChannelHandlerContext, handler: ChannelHandler) {
      resp.setStatus(HttpResponseStatus.OK)
      handshakeHeaders.map { case (header, value) =>
        resp.headers().add(header, value)
      }
      ctx.channel.write(resp)
      val pipeline = ctx.channel.pipeline
      pipeline.replace("handler", "ssehandler", handler)
    }
  }
}

class Handler {

}
