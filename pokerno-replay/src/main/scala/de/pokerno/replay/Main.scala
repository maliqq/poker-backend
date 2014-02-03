package de.pokerno.replay

import jline.console.ConsoleReader
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import de.pokerno.backend.gateway.{Http, http}

case class Config(file: Option[String] = None, http: Boolean = false)

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
  
  val system = ActorSystem("poker-replayer")
  val gw = system.actorOf(Props(classOf[Http.Gateway]), "http-dispatcher")
  val replayer = system.actorOf(Props(classOf[Replayer], gw), "replayer")
  
  def startHttpServer(gw: ActorRef) = {
    val server = new http.Server(gw,
      http.Config(
          port = 8080,
          webSocket = Right(true),
          handlers = List(("api-handler", () => new ApiHandler(replayer)))
      )
    )
    server.start
  }

  def main(args: Array[String]) {
    parser.parse(args, config) foreach { c =>
      if (c.http)
        startHttpServer(gw)
      else {
        startHttpServer(gw)
        
        if (c.file.isDefined)
          parse(c.file.get)
        else {
          val consoleReader = new ConsoleReader
          consoleReader.setExpandEvents(false)
          while (true) {
            val filename = consoleReader.readLine("Enter path to scenario >>> ")
            if (filename == "exit") System.exit(0)
            if (filename != "") parse(filename)
          }
        }
      }
    }
    //system.shutdown()
  }
  
  def parse(filename: String) {
    try {
      val src = scala.io.Source.fromFile(filename)
      val scenario = Scenario.parse(src)
      replayer ! Replayer.Replay(scenario)

    } catch {
      case _: java.io.FileNotFoundException =>
        Console printf("[ERROR] file not found\n")

      case err: ReplayError =>
        Console printf("[ERROR] %s%s%s\n", Console.RED, err.getMessage, Console.RESET)
      
      case err: Exception =>
        Console print("[ERROR]: ")
        Console printf("%s", Console.RED)
        err.printStackTrace
        Console printf("%s\n", Console.RESET)
    }
  }
  
}
