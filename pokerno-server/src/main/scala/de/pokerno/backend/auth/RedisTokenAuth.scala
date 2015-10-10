package de.pokerno.backend.auth

import de.pokerno.backend.gateway.http.AuthService
import redis.clients.jedis.Jedis
import java.net.InetSocketAddress

class RedisTokenAuth(addr: InetSocketAddress) extends AuthService {
  val client = new Jedis(addr.getHostString(), addr.getPort())

  def authorize(secret: String): Option[String] = {
    val result = client.get(secret + ":player")
    if (result == null) None else Some(result)
  }
}
