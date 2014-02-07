package de.pokerno.backend.gateway
import akka.actor.{ Actor, ActorLogging }
import akka.{ zeromq ⇒ zmq }
import de.pokerno.protocol.{ msg ⇒ message, Codec ⇒ codec }

object Zeromq {

  final val defaultHost = "0.0.0.0"
  final val defaultPort = 5555

  case class Config(
      var host: String = defaultHost,
      var port: Int = defaultPort,
      var topic: String = "") {

    def address = f"tcp://$host:$port"

  }
}

class Zeromq(config: Zeromq.Config) extends Actor with ActorLogging {
  import context._

  final val socketType = zmq.SocketType.Pub

  val address = config.address
  val socket = zmq.ZeroMQExtension(system).newSocket(socketType, zmq.Bind(address))

  def receive = {
    case msg: message.Message ⇒
      socket ! encode(msg)
    case msg ⇒
      log info ("unhandled: %s" format (msg))
  }

  private def encode(msg: message.Message) = codec.Protobuf.encode(msg)
}
