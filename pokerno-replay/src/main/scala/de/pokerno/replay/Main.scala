package de.pokerno.replay

import jline.console.ConsoleReader
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import com.typesafe.config.ConfigFactory
import de.pokerno.backend.Gateway
import de.pokerno.protocol.{Codec => codec}
import de.pokerno.gameplay.{Notification, Route => route}
import de.pokerno.backend.gateway.{ Http, http }

private[replay] case class Config(file: Option[String] = None, http: Boolean = false)

private[replay] class Main {
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

object Main {
  //
  //  val config = ConfigFactory.parseString(
  //    """
  //      akka {
  //        loglevel = "DEBUG"
  //        actor {
  //          debug {
  //            //receive = on
  //            //unhandled = on
  //            //lifecycle = on
  //          }
  //        }
  //      }
  //    """)
  //
  //  val actorSystemConfig = ConfigFactory.load(config)

  val parser = new scopt.OptionParser[Config]("poker-console") {
    opt[Unit]("http") text "Start HTTP server" action { (value, c) ⇒
      c.copy(http = true)
    }
    opt[String]('f', "file") text "Load scenario from file" action { (value, c) ⇒
      c.copy(file = Some(value))
    }

    help("help") text "Help"
  }

  val config = Config()

  def main(args: Array[String]) {
    parser.parse(args, config) foreach { c ⇒
      val app = new Main
      app.startHttpServer()

      if (c.http) {}
      else {
        if (c.file.isDefined)
          app.parse(c.file.get)
        else {
          val consoleReader = new ConsoleReader
          consoleReader.setExpandEvents(false)
          while (true) {
            val filename = consoleReader.readLine("Enter path to scenario >>> ")
            if (filename == "exit") System.exit(0)
            if (filename != "") app.parse(filename)
          }
        }
      }
    }
  }

}
