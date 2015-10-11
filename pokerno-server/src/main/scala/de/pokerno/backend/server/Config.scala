package de.pokerno.backend.server

import de.pokerno.backend.{ gateway ⇒ gw }
import java.net.InetSocketAddress

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

import de.pokerno.backend.auth._

object Config {
  final val mapper = new com.fasterxml.jackson.databind.ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def from(f: java.io.InputStream): Config =
    mapper.readValue(f, classOf[Config])

  @JsonCreator
  case class Broadcast(
    // redis brodcast address
    redis: Option[String] = None
  )

  @JsonCreator
  case class Services(
    syncUrl: String,
    paymentsAddr: String = null
  )

  @JsonCreator
  case class Http(
      // Netty port
      port: Int = gw.http.Server.defaultPort,
      // web socket path
      webSocket: Either[String, Boolean] = Right(false),
      // event source path
      eventSource: Either[String, Boolean] = Right(false)
  )
}

case class Config(
    id: java.util.UUID = null,
    host: String = "localhost",
    broadcast: Option[Config.Broadcast] = None,
    services: Config.Services = Config.Services("http://localhost:3000"),
    redis: Option[String] = None,

    apiEnabled: Boolean = false,
    rpcEnabled: Boolean = false,
    authEnabled: Boolean = false,
    eventSourceEnabled: Boolean = false,
    webSocketEnabled: Boolean = false,

    var http: Option[Config.Http] = None,

    var api: Option[String] = None,

    var rpc: Option[String] = None) {

  import de.pokerno.util.HostPort._

  def apiAddress: Option[InetSocketAddress] = {
    if (!api.isDefined && !apiEnabled) return None
    implicit val defaultHost = "localhost"
    implicit val defaultPort = 8087
    Some(api.get)
  }

  def rpcAddress: Option[InetSocketAddress] = {
    if (!rpc.isDefined && !rpcEnabled) return None
    implicit val defaultHost = "localhost"
    implicit val defaultPort = 9091
    Some(rpc.get)
  }

  def httpConfig = http.getOrElse(Config.Http())
  if (webSocketEnabled) http = Some(httpConfig.copy(
    webSocket = Right(true)
  ))
  if (eventSourceEnabled) http = Some(httpConfig.copy(
    eventSource = Right(true)
  ))

  def authService: Option[gw.http.AuthService] = {
    if (!authEnabled || !redis.isDefined) return None
    implicit val defaultHost = "0.0.0.0"
    implicit val defaultPort = 6379
    Some(new RedisTokenAuth(redis.get))
  }

}
