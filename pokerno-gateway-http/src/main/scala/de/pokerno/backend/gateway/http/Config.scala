package de.pokerno.backend.gateway.http

import io.netty.channel.ChannelHandlerAdapter

object Config {
  final val defaultPort = 8081
  
  def default: Config = Config(defaultPort)
}

case class Config(
    port: Int,
    eventSource: Either[EventSource.Config, Boolean] = Right(false),
    webSocket: Either[WebSocket.Config, Boolean] = Right(false),
    handlers: List[Tuple2[String, () ⇒ ChannelHandlerAdapter]] = List.empty) {

  def eventSourceConfig: Option[EventSource.Config] = eventSource match {
    case Right(true) ⇒ Some(EventSource.Config.default)
    case Left(c)     ⇒ Some(c)
    case _           ⇒ None
  }

  def webSocketConfig: Option[WebSocket.Config] = webSocket match {
    case Right(true) ⇒ Some(WebSocket.Config.default)
    case Left(c)     ⇒ Some(c)
    case _           ⇒ None
  }
  
  override def toString = {
    val s = new StringBuffer
    s.append(f"port=$port")
    webSocket match {
      case Right(true) => s.append(" ✓websocket")
      case Left(c) => s.append(" websocket=%s".format(c))
      case _ =>
    }
    eventSource match {
      case Right(true) => s.append(" ✓eventsource")
      case Left(c) => s.append(" eventsource=%s".format(c))
      case _ =>
    }
    s.toString
  }
}