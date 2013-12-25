package de.pokerno.backend.gateway
import akka.actor.Actor
import akka.zeromq._

object Zeromq {
  final val DefaultAddr = "tcp://0.0.0.0:5555"

  case class Config(
    val address: String = DefaultAddr
  )
}

class Zeromq(config: Zeromq.Config) extends Actor {
  import context._

  final val socketType = SocketType.Pub

  val address = config.address
  val socket = ZeroMQExtension(system).newSocket(socketType, Bind(address))

  def receive = {
    case _ ⇒
  }
}
