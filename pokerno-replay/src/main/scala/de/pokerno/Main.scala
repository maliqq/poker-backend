package de.pokerno

import jline.console.ConsoleReader

private[pokerno] case class Config(file: Option[String] = None, http: Boolean = false)

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
      val app = new replay.App
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
