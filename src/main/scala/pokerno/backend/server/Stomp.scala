package pokerno.backend.server

import asia.stampy
import scala.concurrent.duration._

object Stomp {
  
  object Config {
    val host = "localhost"
    val port = 1234
    val heartbeat = 1 second
  }
  
}
