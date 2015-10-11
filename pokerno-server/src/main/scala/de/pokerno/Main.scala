package de.pokerno

import org.slf4j.LoggerFactory

import java.net.URL
import java.io.{FileInputStream, BufferedReader, InputStreamReader}

import de.pokerno.gameplay._
import de.pokerno.backend.{ gateway ⇒ gw }
import akka.actor.{ ActorSystem, Props }
import de.pokerno.backend.server.{Config, Node}

private[pokerno] case class Options(
  val configFile: Option[String] = None,
  val restoreFile: Option[String] = None,
  val config: Config = Config())

object Main {
  import collection.JavaConversions._
  val log = LoggerFactory.getLogger(getClass)

  private val optionParser = new scopt.OptionParser[Options]("poker-server") {
    // -c /etc/node.json
    opt[String]('c', "config") text "Path to config file" action { (value, c) ⇒
      c.copy(configFile = Some(value))
    }

    // -r /tmp/restore.json
    opt[String]('r', "restore") text "restore node from file" action { (value, c) =>
      c.copy(restoreFile = Some(value))
    }

    // -r /tmp/restore.json
    opt[String]('u', "sync") text "sync node from url" action { (value, c) =>
      c.copy(config = c.config.copy(
        syncUrl = value
      ))
    }

    // --host node1.localhost
    opt[String]('h', "host") text "Node hostname" action { (value, c) ⇒
      c.copy(config = c.config.copy(host = value))
    }

    opt[String]("id") text "Node id" action { (value, c) ⇒
      c.copy(config = c.config.copy(id = java.util.UUID.fromString(value)))
    }

    // --http-port 8080
    opt[Int]("http-port") text "HTTP port" action { (value, c) ⇒
      c.copy(config = c.config.copy(
        http = Some(c.config.httpConfig.copy(port = value))
      ))
    }

    // --websocket
    opt[Unit]("websocket") text "With WebSocket" action { (value, c) ⇒
      val config = c.config.httpConfig
      c.copy(config = c.config.copy(
        webSocketEnabled = true
      ))
    }

    // --websocket-path /_ws
    opt[String]("websocket-path") text "WebSocket path" action { (value, c) ⇒
      val config = c.config.httpConfig
      c.copy(config = c.config.copy(
        http = Some(c.config.httpConfig.copy(webSocket = Left(value)))
      ))
    }

    // --eventsource
    opt[Unit]("eventsource") text "With EventSource" action { (value, c) ⇒
      val config = c.config.httpConfig
      c.copy(config = c.config.copy(
        eventSourceEnabled = true
      ))
    }

    // --eventsource-path /_es
    opt[String]("eventsource-path") text "EventSource path" action { (value, c) ⇒
      val config = c.config.httpConfig
      c.copy(config = c.config.copy(
        http = Some(c.config.httpConfig.copy(eventSource = Left(value)))
      ))
    }

    // --rpc
    opt[Unit]("rpc") text "RPC with default options" action { (value, c) ⇒
      c.copy(config = c.config.copy(
        rpcEnabled = true
      ))
    }

    // --rpc-addr 192.168.1.1:8081
    opt[String]("rpc-addr") text "RPC Address" action { (value, c) ⇒
      c.copy(config = c.config.copy(
        rpc = Some(value)
      ))
    }


    // --http-api
    opt[Unit]("http-api") text "HTTP API" action { (value, c) ⇒
      val config = c.config.api
      if (config.isDefined) c // skip
      else c.copy(config = c.config.copy(
        apiEnabled = true
      ))
    }

    // --redis
    opt[String]("redis") text "Redis address to connect for token exchange" action { (value, c) ⇒
      c.copy(config = c.config.copy(
        redis = Some(value)
      ))
    }

    // --enable-auth
    opt[Unit]("enable-auth") text "Enable token based authentication" action { (value, c) ⇒
      c.copy(config = c.config.copy(
        authEnabled = true
      ))
    }

    // --http-api-port 3000
    opt[Int]("http-api-port") text "HTTP API port" action { (value, c) ⇒
      c.copy(config = c.config.copy(
          api = Some(":"+value.toString)
      ))
    }
//
//    // --stomp
//    opt[Unit]("stomp") text "STOMP with default options" action { (value, c) ⇒
//      c.copy(config = c.config.copy(
//        stomp = Some(c.config.stompConfig)
//      ))
//    }
//
//    // --stomp-port 8082
//    opt[Int]("stomp-port") text "STOMP port" action { (value, c) ⇒
//      c.copy(config = c.config.copy(
//        stomp = Some(c.config.stompConfig.copy(port = value))
//      ))
//    }
//
//    // --zeromq
//    opt[Unit]("zeromq") text "ZeroMQ with default options" action { (value, c) ⇒
//      c.copy(config = c.config.copy(
//        zeromq = Some(c.config.zeromqConfig)
//      ))
//    }
//
//    // --zeromq-port
//    opt[Int]("zeromq-port") text "ZeroMQ port" action { (value, c) ⇒
//      val config = c.config.zeromqConfig
//      c.copy(config = c.config.copy(
//        zeromq = Some(config.copy(port = value))
//      ))
//    }
//    // --zeromq-host
//    opt[String]("zeromq-host") text "ZeroMQ host" action { (value, c) ⇒
//      val config = c.config.zeromqConfig
//      c.copy(config = c.config.copy(
//        zeromq = Some(config.copy(host = value))
//      ))
//    }
//    // --zeromq-topic "updates"
//    opt[String]("zeromq-topic") text "ZeroMQ subscribe topic" action { (value, c) ⇒
//      val config = c.config.zeromqConfig
//      c.copy(config = c.config.copy(
//        zeromq = Some(config.copy(topic = value))
//      ))
//    }

    // --help
    help("help") text "Help"
  }

  val options = Options()

  def main(args: Array[String]) {
    optionParser.parse(args, options) map { opts ⇒
      // read config
      val config: Config = opts.configFile.map { path =>
        val f = new java.io.FileInputStream(path)

        try Config.from(f)
        catch {
          case _: java.io.FileNotFoundException ⇒
            log.error("Config file not found!")
            null

          case e: com.fasterxml.jackson.core.JsonParseException ⇒
            log.error("Invalid JSON: {}", e.getMessage)
            null
        }

      }.getOrElse(opts.config)

      // start
      val node = Node.start(config, opts.restoreFile)
    }
  }

}
