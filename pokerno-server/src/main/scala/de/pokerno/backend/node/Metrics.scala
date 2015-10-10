package de.pokerno.backend.node

import akka.actor.{Actor, ActorLogging}
import de.pokerno.backend.Gateway
import com.codahale.metrics._
import concurrent.duration._
import java.util.concurrent.TimeUnit
import de.pokerno.data.pokerdb.thrift

//class PokerDBReporter(
//    registry: MetricRegistry,
//    filter: MetricFilter,
//    rateUnit: TimeUnit,
//    durationUnit: TimeUnit) extends ScheduledReporter(registry, "poker-db-room-reporter", filter, rateUnit, durationUnit) {
//  import java.util.SortedMap
//
//  override def report(gauges: SortedMap[String, Gauge[_]],
//                     counters: SortedMap[String, Counter],
//                     histograms: SortedMap[String, Histogram],
//                     meters: SortedMap[String, Meter],
//                     timers: SortedMap[String, Timer]) {
//  }
//}

sealed class Metrics {
  final val registry = new MetricRegistry

  val totalConnections    = registry.counter("total_connections")
  val playerConnections   = registry.counter("player_connections")
  val offlinePlayers      = registry.counter("offlinePlayers")
  val connects            = registry.meter("connects")
  val disconnects         = registry.meter("disconnects")
  val messagesReceived    = registry.meter("messages_received")

  //val messagesBroadcasted = registry.meter("messages_broadcasted")
  //val messagesSent        = registry.meter("messages_sent")
}

trait MetricsReporter {
  def report()
}

abstract class MetricsHandler extends MetricsReporter {
  val metrics = new Metrics
  def registry = metrics.registry

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
