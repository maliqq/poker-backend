package de.pokerno.backend.server

import de.pokerno.gameplay._
import de.pokerno.backend.{gateway => gw}
import de.pokerno.model._
import akka.actor.{ ActorSystem, Props }
//import de.pokerno.backend.server.Config

case class Options(
    val configFile: Option[String] = None,
    val config: Config = Config()
    )

object Main {
  private val optionParser = new scopt.OptionParser[Options]("poker-server") {
    opt[String]('c', "config") text ("Path to config file") action { (value, c) =>
      c.copy(configFile = Some(value))
    }

    opt[Int]("rpc-port") text("RPC Port") action { (value, c) =>
      val config = c.config.withRpc
      config.getRpc.port = value

      c.copy(config = config)
    }
    
    opt[Int]("stomp-port") text("STOMP port") action { (value, c) =>
      val config = c.config.withStomp
      config.getStomp.port = value
      
      c.copy(config = config)
    }
    
    opt[String]("http-host") text("HTTP host") action { (value, c) =>
      val config = c.config.withHttp
      config.getHttp.host = value
      
      c.copy(config = config)
    }
    
    opt[Int]("http-port") text("HTTP port") action { (value, c) =>
      val config = c.config.withHttp
      config.getHttp.port = value
      
      c.copy(config = config)
    }

//    opt[Int]("http-api-port") text("HTTP API port") action { (value, c) =>
//      val config = c.config.withHttp
//      config.getHttp.withApi.getApi.port = value
//      
//      c.copy(config = config)
//    }

    opt[String]("http-api-path") text("HTTP API path") action { (value, c) =>
      val config = c.config.withHttp
      config.getHttp.withApi.getApi.path = value
      
      c.copy(config = config)
    }
    
//    opt[Int]("http-websocket-port") text("HTTP WebSocket port") action { (value, c) =>
//      val config = c.config.withHttp
//      config.getHttp.withWebSocket.getWs.port = value
//      c.copy(config = config)
//    }
    
    opt[String]("http-websocket-path") text("HTTP WebSocket path") action { (value, c) =>
      val config = c.config.withHttp
      config.getHttp.withWebSocket.getWs.path = value
      
      c.copy(config = config)
    }
    
//    opt[Int]("http-eventsource-port") text("HTTP EventSource port") action { (value, c) =>
//      val config = c.config.withHttp
//      config.getHttp.withEventSource.getEs.port = value
//      
//      c.copy(config = config)
//    }
    opt[String]("http-eventsource-path") text("HTTP EventSource path") action { (value, c) =>
      val config = c.config.withHttp
      config.getHttp.withEventSource.getEs.path = value
      
      c.copy(config = config)
    }
    
    opt[Int]("zeromq-port") text("ZeroMQ port") action { (value, c) =>
      val config = c.config.withZmq
      config.getZmq.port = value
      
      c.copy(config = config)
    }
    opt[String]("zeromq-host") text("ZeroMQ host") action { (value, c) =>
      val config = c.config.withZmq
      config.getZmq.host = value
      
      c.copy(config = config)
    }
    
    opt[String]("zeromq-topic") text("ZeroMQ subscribe topic") action { (value, c) =>
      val config = c.config.withZmq
      config.getZmq.topic = value
      
      c.copy(config = config)
    }
    
    help("help") text("Help")
  }

  val options = Options()
  
  def main(args: Array[String]) {
    optionParser.parse(args, options) map { opts =>
      var config: Config = null
      
      if (opts.configFile.isDefined) {
        val f = new java.io.FileInputStream(opts.configFile.get)
        
        try config = Config.from(f)
        catch {
          case _: java.io.FileNotFoundException =>
            Console printf("Config file not found!")
            System exit(1)
          
          case e: com.fasterxml.jackson.core.JsonParseException =>
            Console printf("Invalid JSON: %s", e.getMessage)
            System exit(1)
        }
      } else config = opts.config
      
      Node.start(config)
    }
  }

}
