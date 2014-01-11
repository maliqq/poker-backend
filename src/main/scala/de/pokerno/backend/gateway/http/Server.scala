package de.pokerno.backend.gateway.http

import akka.actor.{Actor, Props, ActorSystem, ActorRef}

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{Channel, ChannelOption, ChannelInitializer}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpRequestDecoder, HttpResponseEncoder}
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.util.concurrent.{FutureTask, Callable}

class Hub extends Actor {
  import context._

  override def preStart = {
    Console printf("Started Hub\n")
  }
  
  import scala.concurrent.duration._
  
  case class Tick(conn: Connection)
  
  def receive = {
    case Gateway.Connect(conn) =>
      Console printf("%s connected!\n", conn.remoteAddr)
      system.scheduler.schedule(0 milliseconds, 1 second, self, Tick(conn))
    
    case Tick(conn) =>
      Console printf("tick!\n")
      conn.write(EventSource.Packet("hello!"))
    
    case Gateway.Disconnect(conn) =>
      Console printf("disconnected!\n")
  }
}

object Server {
  val system = ActorSystem("server")
  val gw = system.actorOf(Props(classOf[Hub]))
  
  def main(args: Array[String]) {
    val s = Server(8080, gw)
    val t = new Thread(s.start, "eventsource-server")
    t.run
  }
}

import org.slf4j.LoggerFactory

case class Server(val port: Int, gw: ActorRef) {
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
      p.addLast("http-eventsource-handler", new EventSource.Handler(gw))
    }
  }
  
  private def bootstrap: ServerBootstrap = boot.group(bossGroup, workerGroup)
    .channel(classOf[NioServerSocketChannel])
    //.option(ChannelOption.SO_KEEPALIVE.asInstanceOf[ChannelOption[Any]], true)
    //.option(ChannelOption.TCP_NODELAY.asInstanceOf[ChannelOption[Any]], true)
    .childHandler(initializer)
  
  def run {
    if (isActive) throw new IllegalStateException("Server already running!")
    
    channel = bootstrap.bind(port).sync.channel
    channel.closeFuture.sync
  }
  
  def stop {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
  
  def start = new FutureTask[Server](new Callable[Server] {
    override def call = {
      try run finally stop
      Server.this
    }
  })
}
