package de.pokerno.backend.broadcast

import de.pokerno.protocol.{GameEvent => Codec}
import de.pokerno.util.HostPort._
import de.pokerno.backend.Broadcast

case class Zeromq(bind: String) extends Broadcast {
  import org.zeromq.ZMQ

  def this(host: String, port: Int) = this(f"tcp://$host:$port")
  def this(addr: java.net.InetSocketAddress) = this(addr.getHostName, addr.getPort)

  val context = ZMQ.context(1) // 1 thread
  val socket = context.socket(ZMQ.PUB)
  socket.bind(bind)

  def broadcast(topic: String, msg: Broadcast.Message) = {
    socket.sendMore(topic)
    socket.send(msg)
  }
}
