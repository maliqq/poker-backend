package de.pokerno.backend.gateway.stomp

import io.netty.util.CharsetUtil
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{Channel, ChannelOption, ChannelInitializer}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}
import io.netty.channel.socket.nio.NioServerSocketChannel

class Server(port: Int) {
  private lazy val bossGroup = new NioEventLoopGroup
  private lazy val workerGroup = new NioEventLoopGroup
  private lazy val boot = new ServerBootstrap
  
  def initializer = new Initializer
  
  class Initializer extends ChannelInitializer[SocketChannel] {
    override def initChannel(ch: SocketChannel) {
      val p = ch.pipeline
      
      p.addLast("stomp-decoder", new Decoder)
      p.addLast("string-encoder", new StringEncoder(CharsetUtil.UTF_8))
      p.addLast("string-decoder", new StringDecoder(CharsetUtil.UTF_8))
      p.addLast("stomp-handler", new Handler)
    }
  }
  
  private def bootstrap: ServerBootstrap = boot.group(bossGroup, workerGroup)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(initializer)
    
  def shutdown {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
  
  def start {
    val channel = bootstrap.bind(port).sync.channel
    channel.closeFuture.sync
  }
  
}
