package de.pokerno.backend.server.node

import de.pokerno.backend.gateway.http.AuthService
import akka.actor.{ActorRef, ActorSystem, Props}

class Bootstrap(node: ActorRef)(implicit val system: ActorSystem) {
  
  import Console.{printf => log}
  
  def withRpc(host: String, port: Int) {
    log("starting rpc at %s:%d\n", host, port)
    Service(node, new java.net.InetSocketAddress(host, port))
  }
  
  import de.pokerno.backend.{gateway => gw}
  import de.pokerno.backend.Gateway
  
  def withHttp(httpConfig: gw.http.Config, authService: Option[AuthService]) {
    
    val httpGateway = system.actorOf(Props(classOf[gw.Http.Gateway], node, Gateway), name = "http-gateway")

    log("starting HTTP server with config: %s\n", httpConfig)
    val server = new gw.http.Server(httpGateway, authService, httpConfig)
    server.start
  }
  
  def withApi(host: String, port: Int) {
    import spray.can.Http
    log("starting http api at %s:%d\n", host, port)
    val httpApi = system.actorOf(Props(classOf[Api], node), name = "http-api")
    akka.io.IO(Http) ! Http.Bind(httpApi, host, port = port)
  }
  
}
