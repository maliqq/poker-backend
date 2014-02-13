package de.pokerno.replay

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import com.typesafe.config.ConfigFactory
import de.pokerno.backend.Gateway
import de.pokerno.protocol.{Codec => codec}
import de.pokerno.gameplay.{Notification, Route => route}
import de.pokerno.backend.gateway.{ Http, http }

class App {
  val system = ActorSystem("poker-replayer")

  // node emulation
  val node = system.actorOf(Props(new Actor {
    val roomConnections = collection.mutable.HashMap[String, collection.mutable.ListBuffer[http.Connection]]()
    def receive = {
      case Gateway.Connect(conn) =>
        conn.room.map { id =>
          if (!roomConnections.contains(id))
            roomConnections(id) = collection.mutable.ListBuffer()
          roomConnections(id) += conn
        }

      case Gateway.Disconnect(conn) =>
        conn.room.map { id =>
          if (roomConnections.contains(id))
            roomConnections(id) -= conn
        }

      case Notification(msg, from, to) =>
        from match {
          case route.One(id) =>
            val data = codec.Json.encode(msg)
            roomConnections.get(id) map { conns =>
              conns.map { _.send(data) }
            }
            
          case _ =>
        }
    }
  }))

  val gw = system.actorOf(Props(classOf[Http.Gateway], Some(node)), "http-dispatcher")
  val replayer = system.actorOf(Props(classOf[Replayer], node), "replayer")

  def startHttpServer() {
    val server = new http.Server(gw,
      http.Config(
        port = 8080,
        webSocket = Right(true),
        handlers = List(("api-handler", () ⇒ new ApiHandler(replayer)))
      )
    )
    server.start
  }

  import de.pokerno.util.ConsoleUtils._

  def parse(filename: String) {
    try {
      val src = scala.io.Source.fromFile(filename)
      val scenario = Scenario.parse(filename, src)
      replayer ! Replayer.Replay(scenario)

    } catch {
      case err: java.io.FileNotFoundException ⇒
        error(err)

      case err: ReplayError ⇒
        error(err)

      case err: Throwable ⇒
        fatal(err)
    }
  }
}
