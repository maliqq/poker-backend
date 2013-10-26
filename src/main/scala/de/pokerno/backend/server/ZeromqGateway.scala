package de.pokerno.backend.server

import akka.actor.{ Actor, ActorSystem }
import akka.zeromq._

object ZeromqGateway {
  case object Config {
    val address = "tcp://0.0.0.0:5555"
  }
}

class ZeromqGateway extends Actor {
  import context._

  final val socketType = SocketType.Pub

  val address = ZeromqGateway.Config.address
  val socket = ZeroMQExtension(system).newSocket(socketType, Bind(address))

  def receive = {
    case _ ⇒
  }
}
