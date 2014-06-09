package de.pokerno.replay

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import com.typesafe.config.ConfigFactory
import de.pokerno.protocol.GameEvent
import de.pokerno.gameplay.{Notification, Route => route}
import de.pokerno.backend.Gateway
import de.pokerno.backend.gateway.{ Http, http }

class App {
  implicit val system = ActorSystem("poker-replayer")

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
            val data = GameEvent.encode(msg)
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
    val api = system.actorOf(Props(classOf[Api]), name = "api")
    akka.io.IO(spray.can.Http) ! spray.can.Http.Bind(api, "localhost", port = 8082)
    
    val server = new http.Server(gw, http.Config(port = 8083, webSocket = Right(true)))
    server.start
  }

  def parse(filename: String) {
    try {
      val src = scala.io.Source.fromFile(filename)
      val scenario = Scenario.parse(filename, src)
      replayer ! Replayer.Replay(scenario)

    } catch {
      case err: java.io.FileNotFoundException ⇒
        Console printf("%s\n", err.getMessage())

      case err: ReplayError ⇒
        Console printf("Replay error: %s\n", err.getMessage())

      case err: Throwable ⇒
        Console printf("Error: %s\n", err.getMessage())
    }
  }
}
