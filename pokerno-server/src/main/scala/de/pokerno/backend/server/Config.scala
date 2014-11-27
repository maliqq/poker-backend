package de.pokerno.backend.server

import de.pokerno.backend.{ gateway ⇒ gw }
import java.net.InetSocketAddress

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}

object Config {
  final val defaultHost = "0.0.0.0"
  
  import de.pokerno.util.HostPort._

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

  @JsonCreator def fromJson(
    @JsonProperty("id") id: String,
    @JsonProperty("host") host: String,
    @JsonProperty("dbProps") dbProps: String,
    @JsonProperty("redis") redis: String,
    @JsonProperty("rpc") _rpc: String,
    @JsonProperty("authEnabled") authEnabled: Boolean = false,
    @JsonProperty("apiEnabled") apiEnabled: Boolean = false,
    @JsonProperty("rpcEnabled") rpcEnabled: Boolean = false,
    @JsonProperty("websocketEnabled") websocketEnabled: Boolean = false
  ) = {
    val rpc = Option(_rpc)
    var c = new Config(java.util.UUID.fromString(id), host,
        dbProps = Option(dbProps),
        authEnabled = authEnabled,
        redis = Option(redis).map { addr => Redis(addr) },
        api = if (apiEnabled) Some(
            Config.Http.Api.default
          ) else None,
        rpc = if (rpc.isDefined) Some(
            Config.Rpc(rpc.get)
          ) else if (rpcEnabled) Some(
            Config.Rpc.default
          ) else None
      )
    if (websocketEnabled)
      c = c.copy(
          http = Some(c.httpConfig.copy(
              webSocket = Right(true)
              ))
          )
    c
  }
}

case class Config(
    id: java.util.UUID = null,
    host: String = "localhost",
    authEnabled: Boolean = false,
    redis: Option[Config.Redis] = None,
    http: Option[gw.http.Config] = None,
    api: Option[Config.Http.Api] = None,
    rpc: Option[Config.Rpc] = None,
    dbProps: Option[String] = None) {
  
  def apiConfig =
    api.getOrElse(Config.Http.Api.default)
  
  def apiAddress: Option[InetSocketAddress] = api.map { c =>
    new InetSocketAddress(host, c.port)
  }
  
  def httpConfig =
    http.getOrElse(gw.http.Config.default)

  def rpcConfig =
    rpc.getOrElse(Config.Rpc.default)
}
