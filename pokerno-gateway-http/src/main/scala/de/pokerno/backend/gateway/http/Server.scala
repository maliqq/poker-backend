package de.pokerno.backend.gateway.http

import akka.actor.{ Actor, ActorRef }

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{ Channel, ChannelOption, ChannelInitializer, ChannelHandlerAdapter, ChannelFutureListener }
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{ HttpObjectAggregator, HttpRequestDecoder, HttpResponseEncoder }
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.util.concurrent.{ FutureTask, Callable }
import io.netty.util.CharsetUtil

case class Server(gw: ActorRef, authService: Option[AuthService], config: Config) {
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
      
      authService.map { s =>
        p.addLast("token-auth-service", new TokenBasedAuthentication(s))
      }

      config.webSocketConfig.foreach { ws ⇒
        p.addLast(WebSocket.Handler.Name, new WebSocket.Handler(ws.path, gw))
      }

      config.eventSourceConfig.foreach { es ⇒
        p.addLast(EventSource.Handler.Name, new EventSource.Handler(es.path, gw))
      }

      config.handlers.map { case (name, handler) ⇒
        p.addLast(name, handler())
      }
    }
  }

  private def bootstrap: ServerBootstrap = boot.group(bossGroup, workerGroup)
    .channel(classOf[NioServerSocketChannel])
    //.option(ChannelOption.SO_KEEPALIVE.asInstanceOf[ChannelOption[Any]], true)
    //.option(ChannelOption.TCP_NODELAY.asInstanceOf[ChannelOption[Any]], true)
    .childHandler(initializer)

  def run() {
    if (isActive) throw new IllegalStateException("Server already running!")

    channel = bootstrap.bind(config.port).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE).sync.channel
    channel.closeFuture.sync
  }

  def shutdown() {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }

  def stop() {
    Console printf "stopping...\n"
    shutdown()
  }

  lazy val task = new FutureTask[Server](new Callable[Server] {
    override def call = {
      try {
        run()
      } finally shutdown()
      Server.this
    }
  })

  def start = {
    val t = new Thread(task, "http-server")
    t.start()
    t
  }
}
