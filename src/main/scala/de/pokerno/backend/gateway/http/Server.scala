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

case class Server(val port: Int, gw: ActorRef) {
  private var channel: Channel = null
  private var boot: ServerBootstrap = null
  
  def isActive = channel != null && channel.isActive
  
  def start = new java.util.concurrent.FutureTask[Server](new java.util.concurrent.Callable[Server] {
    override def call = {
      if (isActive) throw new IllegalStateException("Server already running!")
      
      val bossGroup = new NioEventLoopGroup
      val workerGroup = new NioEventLoopGroup
      
      boot = new ServerBootstrap
      try {
        boot.group(bossGroup, workerGroup)
          .channel(classOf[NioServerSocketChannel])
          //.option(ChannelOption.SO_KEEPALIVE.asInstanceOf[ChannelOption[Any]], true)
          //.option(ChannelOption.TCP_NODELAY.asInstanceOf[ChannelOption[Any]], true)
          .childHandler(new ChannelInitializer[SocketChannel]() {
            override def initChannel(ch: SocketChannel) {
              ch.pipeline.addLast(
                  new HttpRequestDecoder,
                  new HttpObjectAggregator(65536),
                  new HttpResponseEncoder,
                  //new PathHandler("/_events",
                      new EventSource.Handler(gw)
                  //)
                  )
            }
          })
        channel = boot.bind(port).sync.channel
        channel.closeFuture.sync
      } finally {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
      }
      Server.this
    }
  })
}
