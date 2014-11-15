package de.pokerno.backend.server

import de.pokerno.backend.{ gateway ⇒ gw }
import java.net.InetSocketAddress

object Config {
  final val defaultHost = "0.0.0.0"
  
  implicit def string2inetSocketAddress(s: String)(implicit defaultHost: String, defaultPort: Int): InetSocketAddress = {
    if (s.indexOf(":") != -1) {
      val parts = s.split(":", 2)
      val host = if (parts(0) == "") defaultHost else parts(0)
      val port = if (parts(1) == "") defaultPort else Integer.parseInt(parts(1))
      new InetSocketAddress(host, port)
    } else {
      throw new IllegalArgumentException(f"invalid address: $s")
    }
  }
  
  object Http {
    object Api {
      final val defaultPath = "/_api"
      final val defaultPort = 8080
      
      def default: Api = Api(defaultPath, defaultPort)
    }
    case class Api(
      var path: String,
      var port: Int
    )
  }

  object Rpc {
    final implicit val defaultHost = "localhost"
    final implicit val defaultPort = 9091
    def default = Rpc(defaultHost, defaultPort)
    def apply(host: String, port: Int): Rpc = Rpc(new InetSocketAddress(host, port))
    def apply(s: String): Rpc = Rpc(s: InetSocketAddress)
  }
  case class Rpc(addr: InetSocketAddress)
  
  object Redis {
    final implicit val defaultHost = "localhost"
    final implicit val defaultPort = 6379
    def default: Redis = Redis(defaultHost, defaultPort)
    def apply(host: String, port: Int): Redis = Redis(new InetSocketAddress(host, port))
    def apply(s: String): Redis = Redis(s: InetSocketAddress)
  }
  case class Redis(addr: InetSocketAddress)
  
  import com.fasterxml.jackson.databind.ObjectMapper

  def from(f: java.io.InputStream): Config =
    (new ObjectMapper).readValue(f, classOf[Config])
}

case class Config(
    var id: java.util.UUID = java.util.UUID.randomUUID(),
    var host: String = "localhost",
    var authEnabled: Boolean = false,
    var redis: Option[Config.Redis] = None,
    var http: Option[gw.http.Config] = None,
    var api: Option[Config.Http.Api] = None,
    var rpc: Option[Config.Rpc] = None,
    var dbProps: Option[String] = None) {

  def apiConfig =
    api.getOrElse(Config.Http.Api.default)
  
  def httpConfig =
    http.getOrElse(gw.http.Config.default)

  def rpcConfig =
    rpc.getOrElse(Config.Rpc.default)
}
