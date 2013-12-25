package de.pokerno.backend.server

import de.pokerno.gameplay._
import de.pokerno.model._
import akka.actor.{ ActorSystem, Props }

case class Options(
    val configFile: String = "/etc/poker-server/node.json",
    val config: Config = Config()
    )

object Main {
  private val optionParser = new scopt.OptionParser[Options]("poker-server") {
    opt[String]('c', "config") text ("Path to config file") action { (value, c) =>
      c.copy(configFile = value)
    }
    
    opt[String]("http-host") text("HTTP host")
    
    opt[Int]("http-api-port") text("HTTP API port")
    opt[String]("http-api-path") text("HTTP API path")
    
    opt[Int]("http-websocket-port") text("HTTP WebSocket port")
    opt[String]("http-websocket-path") text("HTTP WebSocket path")
    
    opt[Int]("http-eventsource-port") text("HTTP EventSource port")
    opt[String]("http-eventsource-path") text("HTTP EventSource path")
    
    opt[Int]("zeromq-port") text("ZeroMQ port")
    opt[String]("zeromq-host") text("ZeroMQ host")
    opt[String]("zeromq-subscribe-topic") text("ZeroMQ subscribe topic")
  }
  
  val options = Options()
  
  def main(args: Array[String]) {
    optionParser.parse(args, options) map { opts =>
      
    }
  }
}
