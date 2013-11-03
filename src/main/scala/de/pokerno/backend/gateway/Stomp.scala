package de.pokerno.backend.gateway
import scala.concurrent.duration._

object Stomp {

  object Config {
    val host = "localhost"
    val port = 1234
    val heartbeat = 1 second
  }

}
