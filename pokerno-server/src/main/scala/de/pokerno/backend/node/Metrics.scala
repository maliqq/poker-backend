package de.pokerno.backend.node

import akka.actor.{Actor, ActorLogging}
import de.pokerno.backend.Gateway
import com.codahale.metrics._
import concurrent.duration._
import java.util.concurrent.TimeUnit
import de.pokerno.data.pokerdb.thrift
import com.fasterxml.jackson.annotation.JsonGetter

sealed class Metrics {
  private final val registry = new MetricRegistry

  def serializable = registry.getMetrics()

  lazy val totalConnections    = registry.counter("total_connections")
  lazy val playerConnections   = registry.counter("player_connections")
  lazy val offlinePlayers      = registry.counter("offlinePlayers")
  lazy val connects            = registry.meter("connects")
  lazy val disconnects         = registry.meter("disconnects")
  lazy val messagesReceived    = registry.meter("messages_received")

  @JsonGetter("total_connections_count") def totalConnectionsCount = totalConnections.getCount()
  @JsonGetter("player_connections_count") def playerConnectionsCount = playerConnections.getCount()
  @JsonGetter("offline_players_count") def offlinePlayersCount = offlinePlayers.getCount()
  @JsonGetter("connects_rate") def connectsRate = connects.getFifteenMinuteRate()
  @JsonGetter("disconnects_rate") def disconnectsRate = disconnects.getFifteenMinuteRate()
  @JsonGetter("messages_received_rate") def messagesReceivedRate = messagesReceived.getFifteenMinuteRate()

  //val messagesBroadcasted = registry.meter("messages_broadcasted")
  //val messagesSent        = registry.meter("messages_sent")
}

trait MetricsReporter {
  def report()
}

abstract class MetricsHandler extends MetricsReporter {
  val metrics = new Metrics

  def connected(isPlayer: Boolean = false) {
    metrics.totalConnections.inc()
    if (isPlayer) metrics.playerConnections.inc()
    metrics.connects.mark()
  }

  def disconnected(isPlayer: Boolean = false) {
    metrics.totalConnections.dec()
    if (isPlayer) metrics.playerConnections.dec()
    metrics.disconnects.mark()
  }

  def messageReceived() {
    metrics.messagesReceived.mark()
  }
}

//  private def startGraphite() {
//    import com.codahale.metrics.graphite._
//    val graphite = new Graphite(new java.net.InetSocketAddress("localhost", 2003))
//    val reporter = GraphiteReporter.forRegistry(metrics.registry).
//                        prefixedWith("nodes." + nodeId).
//                        convertRatesTo(TimeUnit.SECONDS).
//                        convertDurationsTo(TimeUnit.MILLISECONDS).
//                        filter(MetricFilter.ALL).
//                        build(graphite)
//    reporter.start(1, TimeUnit.MINUTES)
//  }
