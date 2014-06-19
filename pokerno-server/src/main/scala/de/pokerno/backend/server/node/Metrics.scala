package de.pokerno.backend.server.node

import akka.actor.{Actor, ActorLogging}
import de.pokerno.backend.Gateway

class Metrics extends Actor with ActorLogging {
  import com.codahale.metrics._
  val metrics = new MetricRegistry
  
  val totalConnections    = metrics.counter("total_connections")
  val playerConnections   = metrics.counter("player_connections")
  val playersOffline      = metrics.counter("players_offline")
  val connects            = metrics.meter("connects")
  val disconnects         = metrics.meter("disconnects")
  val messagesReceived    = metrics.meter("messages_received")
  //val messagesBroadcasted = metrics.meter("messages_broadcasted")
  //val messagesSent        = metrics.meter("messages_sent")
  
  def receive = {
    case Gateway.Connect(conn) =>
      totalConnections.inc()
      if (conn.player.isDefined)
        playerConnections.inc()
      connects.mark()
    
    case Gateway.Disconnect(conn) =>
      totalConnections.dec()
      if (conn.player.isDefined)
        playerConnections.dec()
      disconnects.mark()
    
    case Gateway.Message(_, _) =>
      messagesReceived.mark()
  }
  
}
