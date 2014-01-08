package de.pokerno.backend.gateway.http

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpRequestDecoder, HttpResponseEncoder}
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.channel.socket.nio.NioServerSocketChannel

class Server(val port: Int, val path: String) extends Runnable {
  def run {
    val bossGroup = new NioEventLoopGroup
    val workerGroup = new NioEventLoopGroup
    try {
      val boot = new ServerBootstrap
      boot.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new ChannelInitializer[SocketChannel]() {
          override def initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(
                new HttpRequestDecoder,
                new HttpObjectAggregator(65536),
                new HttpResponseEncoder,
                new WebSocketServerProtocolHandler(path)
                )
          }
        })
        val ch = boot.bind(port).sync.channel
        ch.closeFuture.sync
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }
}
