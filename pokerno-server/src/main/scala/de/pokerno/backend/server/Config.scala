package de.pokerno.backend.server

import de.pokerno.backend.{ gateway ⇒ gw }
import java.net.InetSocketAddress

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

import de.pokerno.backend.auth._

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

  private abstract class HostPortConverter[T] extends com.fasterxml.jackson.databind.util.StdConverter[String, T] {
    def convert(hostport: String): T
  }

  // RPC
  trait DefaultRpcHostPort {
    final implicit val defaultHost = "localhost"
    final implicit val defaultPort = 9091
  }
  object Rpc extends DefaultRpcHostPort {
    def default = Rpc(defaultHost, defaultPort)
    def apply(host: String, port: Int): Rpc = Rpc(new InetSocketAddress(host, port))
    def apply(s: String): Rpc = Rpc(s: InetSocketAddress)
  }
  private class RpcHostPortConverter extends HostPortConverter[Rpc] with DefaultRpcHostPort {
    def convert(s: String) = Rpc(s: InetSocketAddress)
  }
  @JsonDeserialize(converter = classOf[RpcHostPortConverter])
  case class Rpc(addr: InetSocketAddress)

  // redis
  trait DefaultRedisHostPort {
    final implicit val defaultHost = "localhost"
    final implicit val defaultPort = 6379
  }
  object Redis extends DefaultRedisHostPort {
    def default: Redis = Redis(defaultHost, defaultPort)
    def apply(host: String, port: Int): Redis = Redis(new InetSocketAddress(host, port))
    def apply(s: String): Redis = Redis(s: InetSocketAddress)
  }
  private class RedisHostPortConverter extends HostPortConverter[Redis] with DefaultRedisHostPort {
    def convert(s: String) = Redis(s: InetSocketAddress)
  }
  @JsonDeserialize(converter = classOf[RedisHostPortConverter])
  case class Redis(addr: InetSocketAddress)

  final val mapper = new com.fasterxml.jackson.databind.ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def from(f: java.io.InputStream): Config =
    mapper.readValue(f, classOf[Config])

  @JsonCreator
  case class Broadcast(
    @JsonProperty("redis") redis: Option[String] = None
  )
}

case class Config(
    id: java.util.UUID = null,
    host: String = "localhost",
    authEnabled: Boolean = false,
    broadcast: Option[Config.Broadcast] = None,
    redis: Option[Config.Redis] = None,

    websocketEnabled: Boolean = false,
    var http: Option[gw.http.Config] = None,

    apiEnabled: Boolean = false,
    var api: Option[Config.Http.Api] = None,

    rpcEnabled: Boolean = false,
    var rpc: Option[Config.Rpc] = None) {

  if (apiEnabled) api = Some(
    Config.Http.Api.default
  )

  if (rpcEnabled) rpc = Some(
    Config.Rpc.default
  )

  if (websocketEnabled) http = Some(httpConfig.copy(
    webSocket = Right(true)
  ))

  def apiConfig =
    api.getOrElse(Config.Http.Api.default)

  def apiAddress: Option[InetSocketAddress] = api.map { c =>
    new InetSocketAddress(host, c.port)
  }

  def httpConfig =
    http.getOrElse(gw.http.Config.default)

  def rpcConfig =
    rpc.getOrElse(Config.Rpc.default)

  def authService: Option[gw.http.AuthService] = {
    if (!authEnabled || !redis.isDefined) return None
    Some(new RedisTokenAuth(redis.get.addr))
  }

}
