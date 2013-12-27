package de.pokerno.backend.gateway.eventsource

import io.netty.buffer.ByteBuf
import io.netty.channel.{Channel, ChannelHandler, ChannelHandlerContext}
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.MessageToByteEncoder
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
  
  case class Packet(val data: String,
      val comment: Boolean = false,
      val event: String = null,
      val id: java.lang.Long = null) {
    
    override def toString = {
      var b = new StringBuffer
      var prefix = ""
      
      if (!comment) {
        if (id != null)
          b.append("id: ").append(id.toString)
        if (event != null)
          b.append("event: ").append(event.replaceAll("\n", "")).append("\n")
        prefix = "data"
      }

      data.split("\n").foreach { line =>
        b.append(prefix + ": ").append(line).append("\n")
      }
      
      b.append("\n").toString
    }
  }
  
  class Encoder extends MessageToByteEncoder[Packet] {
    def encode(ctx: ChannelHandlerContext, packet: Packet, buf: ByteBuf) = {
      
    }
  }
}

class Handler {

}
