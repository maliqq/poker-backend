package de.pokerno.backend.server

import de.pokerno.backend.{ gateway ⇒ gw }

object Config {
  final val defaultHost = "127.0.0.1"

  object Http {
    final val defaultPort = 8080

    object Api {
      final val defaultPath = "/_api"
      final val defaultPort = 8080
    }

    case class Api(
      var port: Int = Api.defaultPort,
      var path: String = Api.defaultPath) {
      override def toString = f"$port/$path"
    }
  }

  object Rpc {
    final val defaultHost = "localhost"
    final val defaultPort = 8081
  }

  case class Rpc(
    var host: String = Rpc.defaultHost,
    var port: Int = Rpc.defaultPort) {
    override def toString = f"$host:$port" 
  }

  import com.fasterxml.jackson.databind.ObjectMapper

  def from(f: java.io.InputStream): Config =
    (new ObjectMapper).readValue(f, classOf[Config])
}

case class Config(
    var host: String = "localhost",
    var http: Option[gw.http.Config] = None,
    var rpc: Option[Config.Rpc] = None) {

  def httpConfig =
    http.getOrElse(gw.http.Config())

  def rpcConfig =
    rpc.getOrElse(Config.Rpc())
}
