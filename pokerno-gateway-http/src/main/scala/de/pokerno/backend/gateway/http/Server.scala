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

object Server {
  final val defaultPort = 8081
}

case class Server(
    gw: ActorRef,
    authService: Option[AuthService],
    port: Int = Server.defaultPort,
    eventSource: Either[String, Boolean] = Right(false),
    webSocket: Either[String, Boolean] = Right(false),
    handlers: List[Tuple2[String, () ⇒ ChannelHandlerAdapter]] = List.empty
    ) {
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

      webSocket match {
        case Left(path) => 
          p.addLast(WebSocket.Handler.Name, new WebSocket.Handler(path, gw))
        case Right(true) =>
          p.addLast(WebSocket.Handler.Name, new WebSocket.Handler(WebSocket.defaultPath, gw))
        case _ =>
      }

      eventSource match {
        case Left(path) =>
          p.addLast(EventSource.Handler.Name, new EventSource.Handler(path, gw))
        case Right(true) =>
          p.addLast(EventSource.Handler.Name, new WebSocket.Handler(EventSource.defaultPath, gw))
      }

      handlers.map { case (name, handler) ⇒
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

    channel = bootstrap.bind(port).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE).sync.channel
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
