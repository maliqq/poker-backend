package de.pokerno.backend.server

import de.pokerno.backend.{gateway => gw}

object Config {
  case class ZeroMQ(val host: String, port: Int)
  
  object Http {
    case class Api(val port: Int, val path: String = "/_api")
  }
  
  case class Rpc(val host: String, port: Int)
  
  case class Http(
      val host: String,
      val api: Option[Http.Api],
      val webSocket: Option[gw.http.WebSocket.Config],
      val eventSource: Option[gw.http.EventSource.Config]
      )
}

case class Config(
    val http: Option[Config.Http] = None,
    val rpc: Option[Config.Rpc] = None,
    val zeromq: Option[Config.ZeroMQ] = None
)
