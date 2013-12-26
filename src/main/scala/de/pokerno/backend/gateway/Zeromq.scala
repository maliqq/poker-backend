package de.pokerno.backend.gateway
import akka.actor.{Actor, ActorLogging}
import akka.{zeromq => zmq}
import de.pokerno.backend.{protocol => proto}

object Zeromq {
  final val DefaultAddr = "tcp://0.0.0.0:5555"

  case class Config(
    val address: String = DefaultAddr
  )
}

class Zeromq(config: Zeromq.Config) extends Actor with ActorLogging {
  import context._

  final val socketType = zmq.SocketType.Pub

  val address = config.address
  val socket = zmq.ZeroMQExtension(system).newSocket(socketType, zmq.Bind(address))

  def receive = {
    case msg: proto.Message ⇒
      socket ! encode(msg)
    case msg =>
      log info("unhandled: %s" format(msg))
  }
  
  private def encode(msg: proto.Message) = proto.Codec.Protobuf.encode(msg)
}
