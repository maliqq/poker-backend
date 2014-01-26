package de.pokerno.backend.gateway.http

import akka.actor.{Actor, ActorRef}

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{Channel, ChannelOption, ChannelInitializer}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpRequestDecoder, HttpResponseEncoder}
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.util.concurrent.{FutureTask, Callable}
import io.netty.util.CharsetUtil

case class Config(
    port: Int = Server.defaultPort,
    eventSource: Either[EventSource.Config, Boolean] = Right(false),
    webSocket: Either[WebSocket.Config, Boolean] = Right(false)
  ) {
  
  def eventSourceConfig: Option[EventSource.Config] = eventSource match {
    case Right(true) => Some(EventSource.Config())
    case Left(c) => Some(c)
    case _ => None
  }
  
  def webSocketConfig: Option[WebSocket.Config] = webSocket match {
    case Right(true) => Some(WebSocket.Config())
    case Left(c) => Some(c)
    case _ => None
  }
}

object Server {
  final val defaultPort = 8082
}

case class Server(gw: ActorRef, config: Config) {
  private var channel: Channel = null
  
  def isActive = channel != null && channel.isActive
  
  private lazy val bossGroup = new NioEventLoopGroup
  private lazy val workerGroup = new NioEventLoopGroup
  private lazy val boot = new ServerBootstrap
  
  def initializer = new Initializer
  
  class Initializer extends ChannelInitializer[SocketChannel] {
    override def initChannel(ch: SocketChannel) {
      val p = ch.pipeline
      p.addLast("http-request-decoder", new HttpRequestDecoder)
      p.addLast("http-object-aggregator", new HttpObjectAggregator(65536))
      p.addLast("http-response-encoder", new HttpResponseEncoder)
      
      config.webSocketConfig.foreach { ws =>
        p.addLast(WebSocket.Handler.Name, new WebSocket.Handler(ws.path, gw))
      }
      
      config.eventSourceConfig.foreach { es =>
          p.addLast(EventSource.Handler.Name, new EventSource.Handler(es.path, gw))
      }
    }
  }
  
  private def bootstrap: ServerBootstrap = boot.group(bossGroup, workerGroup)
    .channel(classOf[NioServerSocketChannel])
    //.option(ChannelOption.SO_KEEPALIVE.asInstanceOf[ChannelOption[Any]], true)
    //.option(ChannelOption.TCP_NODELAY.asInstanceOf[ChannelOption[Any]], true)
    .childHandler(initializer)
  
  def run {
    if (isActive) throw new IllegalStateException("Server already running!")
    
    Console printf("starting at :%d...\n", config.port)
    
    channel = bootstrap.bind(config.port).sync.channel
    channel.closeFuture.sync
  }
  
  def shutdown {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
  
  def stop {
    Console printf("stopping...\n")
    shutdown
  }
  
  lazy val task = new FutureTask[Server](new Callable[Server] {
    override def call = {
      try {
        run 
      } finally shutdown
      Server.this
    }
  })
  
  def start = {
    val t = new Thread(task, "http-server")
    t.start
    t
  }
}
