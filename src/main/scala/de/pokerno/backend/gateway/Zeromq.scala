package de.pokerno.backend.gateway
import akka.actor.Actor
import akka.zeromq._

object Zeromq {
  case object Config {
    val address = "tcp://0.0.0.0:5555"
  }
}

class Zeromq extends Actor {
  import context._

  final val socketType = SocketType.Pub

  val address = Zeromq.Config.address
  val socket = ZeroMQExtension(system).newSocket(socketType, Bind(address))

  def receive = {
    case _ ⇒
  }
}
