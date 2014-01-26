package de.pokerno.backend.server

import de.pokerno.backend.{gateway => gw}

object Config {
  final val defaultHost = "localhost"
  
  object Http {
    final val defaultPort = 8080
    
    object Api {
      final val defaultPath = "/_api"
      final val defaultPort = 8080
    }
    
    case class Api(
        var port: Int = Api.defaultPort,
        var path: String = Api.defaultPath)
  }
  
  object Rpc {
    final val defaultHost = "localhost"
    final val defaultPort = 8081
  }
  
  case class Rpc(
      var host: String = Rpc.defaultHost,
      var port: Int = Rpc.defaultPort)
      
  import com.fasterxml.jackson.databind.ObjectMapper

  def from(f: java.io.InputStream): Config =
    (new ObjectMapper).readValue(f, classOf[Config])
}

case class Config(
    var host: String = "localhost",
    var http: Option[gw.http.Config] = None,
    var rpc: Option[Config.Rpc] = None,
    var stomp: Option[gw.Stomp.Config] = None,
    var zeromq: Option[gw.Zeromq.Config] = None
) {
  
  def httpConfig =
    http.getOrElse(gw.http.Config())
  
  def stompConfig =
    stomp.getOrElse(gw.Stomp.Config())
  
  def rpcConfig =
    rpc.getOrElse(Config.Rpc())
  
  def zeromqConfig =
    zeromq.getOrElse(gw.Zeromq.Config())

}
