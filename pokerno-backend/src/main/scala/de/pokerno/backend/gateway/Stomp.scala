package de.pokerno.backend.gateway

import akka.actor.{ Actor, ActorLogging, ActorRef }

object Stomp {

  final val defaultHost = "localhost"
  final val defaultPort = 1234
  final val defaultHb = 1

  case class Config(
    var host: String = "localhost",
    var port: Int = defaultPort,
    var heartbeat: Int = defaultHb)

  class Server extends Actor with ActorLogging {
    override def preStart() {
    }

    def receive = {
      case _ ⇒
    }
  }

}
