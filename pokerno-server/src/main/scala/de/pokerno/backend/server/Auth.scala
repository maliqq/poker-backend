package de.pokerno.backend.server

import de.pokerno.backend.gateway.http.AuthService
import redis.clients.jedis.Jedis

object RedisAuthService {
  def apply(addr: String) = {
    val p = addr.split(":")
    var host = "localhost"
    var port = 6379
    
    if (p.size == 2) {
      host = p(0)
      port = Integer.parseInt(p(1))
    }
    
    new RedisAuthService(host, port)
  }
}

class RedisAuthService(host: String, port: Int) extends AuthService {
  val client = new Jedis(host, port)
  
  def authorize(secret: String): Option[String] = {
    val result = client.get(secret + ":player")
    if (result == "") None else Some(result)
  }
  
  def this(addr: java.net.InetSocketAddress) = this(addr.getHostString(), addr.getPort()) 
}
