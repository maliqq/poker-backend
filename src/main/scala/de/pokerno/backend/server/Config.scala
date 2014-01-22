package de.pokerno.backend.server

import de.pokerno.backend.{gateway => gw}

object Config {
  final val defaultHost = "localhost"
  
  object ZeroMQ {
    final val defaultPort = 5555
  }
  
  case class ZeroMQ(
      var host: String = defaultHost,
      var port: Int = ZeroMQ.defaultPort, 
      var topic: String = "")
  
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
  
  case class Http(
      var host: String = "localhost",
      var port: Int = Http.defaultPort,
      var api: Option[Http.Api] = None,
      var stomp: Option[gw.Stomp.Config] = None,
      var webSocket: Option[gw.http.WebSocket.Config] = None,
      var eventSource: Option[gw.http.EventSource.Config] = None
      ) {
    
    def getApi = api.get
    
    def withApi = {
      if (!api.isDefined) api = Some(Http.Api())
      this
    }
    
    def getStomp = stomp.get
    def withStomp = {
      if (!stomp.isDefined) stomp = Some(gw.Stomp.Config())
      this
    }
    
    def getWs = webSocket.get
    
    def withWebSocket = {
      if (!webSocket.isDefined) webSocket = Some(gw.http.WebSocket.Config())
      this
    }
    
    def getEs = eventSource.get
    
    def withEventSource = {
      if (!eventSource.isDefined) eventSource = Some(gw.http.EventSource.Config())
      this
    }
    
  }

  import com.fasterxml.jackson.databind.ObjectMapper

  def from(f: java.io.InputStream): Config =
    (new ObjectMapper).readValue(f, classOf[Config])
}

case class Config(
    var http: Option[Config.Http] = None,
    var rpc: Option[Config.Rpc] = None,
    var zeromq: Option[Config.ZeroMQ] = None
) {
  
  def getHttp = http.get
  
  def withHttp = {
    if (!http.isDefined) http = Some(Config.Http())
    this
  }
  
  def getRpc = rpc.get
  
  def withRpc = {
    if (!rpc.isDefined) rpc = Some(Config.Rpc())
    this
  }
  
  def getZmq = zeromq.get
  
  def withZmq = {
    if (!zeromq.isDefined) zeromq = Some(Config.ZeroMQ())
    this
  }

}
