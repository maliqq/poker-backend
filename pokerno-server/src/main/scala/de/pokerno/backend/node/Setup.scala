package de.pokerno.backend.node

import de.pokerno.backend.gateway.http.AuthService
import akka.actor.{ActorRef, ActorSystem, Props}
import java.net.InetSocketAddress
import de.pokerno.backend.server.Config

class Setup(node: ActorRef)(implicit val system: ActorSystem) {

  import Console.{printf => log}

  def withRpc(addr: InetSocketAddress) {
    log("starting rpc at %s\n", addr)
    Service(node, addr)
  }

  import de.pokerno.backend.{gateway => gw}
  import de.pokerno.backend.Gateway

  def withHttp(conf: Config.Http, authService: Option[AuthService]) {
    val httpGateway = system.actorOf(Props(classOf[gw.Http.Gateway], node, Gateway), name = "http-gateway")

    log("starting http gateway with config: %s\n", conf)
    val server = new gw.http.Server(httpGateway, authService,
      port = conf.port,
      webSocket = conf.webSocket,
      eventSource = conf.eventSource
    )
    server.start
  }

  def withApi(addr: InetSocketAddress) {
    import spray.can.Http
    log("starting http api at %s\n", addr)
    val httpApi = system.actorOf(Props(classOf[Api], node), name = "http-api")
    akka.io.IO(Http) ! Http.Bind(httpApi, addr.getHostString(), port = addr.getPort())
  }

}
