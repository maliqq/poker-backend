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
  def from(f: java.io.InputStream): Config = mapper.readValue(f, classOf[Config])
}

case class Config(
    id: java.util.UUID = null,
    host: String = "localhost",
    syncUrl: String = "http://localhost:3000",
    payment: Option[String] = None,
    redis: Option[String] = None,
    broadcastRedis: Option[String] = None,

    authEnabled: Boolean = true,
    authSecret: Option[String] = None,

    // http
    httpPort: Option[Int] = None,

    apiEnabled: Boolean = false,
    api: Option[String] = None,

    rpcEnabled: Boolean = false,
    rpc: Option[String] = None,

    eventSourceEnabled: Boolean = false,
    eventSourcePath: Option[String] = None,

    webSocketEnabled: Boolean = true,
    webSocketPath: Option[String] = None
    ) {

  import de.pokerno.util.HostPort._

  def apiAddress: Option[InetSocketAddress] = {
    if (!apiEnabled) return None
    implicit val defaultHost = "localhost"
    implicit val defaultPort = 8087
    Some(api.getOrElse(null))
  }

  def rpcAddress: Option[InetSocketAddress] = {
    if (!rpc.isDefined && !rpcEnabled) return None
    implicit val defaultHost = "localhost"
    implicit val defaultPort = 9091
    Some(rpc.getOrElse(null))
  }

  def paymentAddress: InetSocketAddress = {
    implicit val defaultHost = "localhost"
    implicit val defaultPort = 3031
    payment.getOrElse(null)
  }

  var webSocket: Either[String, Boolean] = Right(webSocketEnabled)
  var eventSource: Either[String, Boolean] = Right(eventSourceEnabled)

  def authService: Option[gw.http.AuthService] = {
//    if (!authEnabled || !redis.isDefined) return None
//    implicit val defaultHost = "0.0.0.0"
//    implicit val defaultPort = 6379
//    Some(new RedisTokenAuth(redis.get))

	authSecret.map { secret =>
      new de.pokerno.backend.auth.JwtAuth(secret)
	} orElse None
  }

}
