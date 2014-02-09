package de.pokerno.backend.rpc

import akka.actor.{Actor, ActorLogging}
import akka.{ zeromq ⇒ zmq }
import de.pokerno.protocol.{ rpc, Codec ⇒ codec }

object Zeromq {
  final val defaultHost = "0.0.0.0"
  final val defaultPort = 5554
  
  case class Config(
      var host: String = defaultHost,
      var port: Int = defaultPort
      ) {
    def address = f"tcp://$host:$port"
  }
}

class Zeromq(config: Zeromq.Config) extends Actor with ActorLogging {
  import context._
  
  final val socketType = zmq.SocketType.Router
  val socket = zmq.ZeroMQExtension(system).newSocket(socketType, zmq.Bind(config.address))

  def receive = {
    case m: zmq.ZMQMessage =>
      
  }
  
  private def decode(msg: Array[Byte]) = codec.Protobuf.decode[rpc.Request](msg)
}
