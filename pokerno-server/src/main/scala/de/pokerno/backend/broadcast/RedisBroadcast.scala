package de.pokerno.backend.broadcast

import de.pokerno.protocol.{GameEvent => Codec}
import de.pokerno.util.HostPort._
import de.pokerno.backend.Broadcast

object Redis {
  implicit val defaultHost = "localhost"
  implicit val defaultPort = 6379
  
  def apply(): Redis = Redis(defaultHost, defaultPort) 
  def apply(s: String): Redis = Redis(s: java.net.InetSocketAddress)
  def apply(addr: java.net.InetSocketAddress) = new Redis(addr.getHostName, addr.getPort)
}

case class Redis(host: String, port: Int) extends Broadcast {
  import de.pokerno.util.HostPort._

  val client = new redis.clients.jedis.Jedis(host, port)

  def broadcast(topic: String, msg: Broadcast.Message) = {
    client.publish(topic, msg)
  }
}
