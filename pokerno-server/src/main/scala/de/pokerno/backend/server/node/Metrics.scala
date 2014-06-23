package de.pokerno.backend.server.node

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

trait Reporting { m: Metrics =>
  def report() {
    val m = thrift.metrics.Node(
        metrics.totalConnections.getCount(),
        metrics.playerConnections.getCount(),
        metrics.offlinePlayers.getCount(),
        thrift.metrics.Meter(mean = metrics.connects.getMeanRate(), rate15 = Some(metrics.connects.getFifteenMinuteRate())),
        thrift.metrics.Meter(mean = metrics.disconnects.getMeanRate(), rate15 = Some(metrics.disconnects.getFifteenMinuteRate())),
        thrift.metrics.Meter(mean = metrics.messagesReceived.getMeanRate(), rate15 = Some(metrics.messagesReceived.getFifteenMinuteRate()))
    )
    pokerdb match {
      case Some(service) => service.reportNodeMetrics(nodeId.toString(), m)
      case _ => println(m)
    }
  }
}

trait Metrics extends Reporting { a: Actor =>
  val nodeId: java.util.UUID
  val pokerdb: Option[thrift.PokerDB.FutureIface]
  
  import context._
//  
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
  
  def startReporting() {
    system.scheduler.schedule(1.minute, 1.minute) {
      report()
    }
    //startGraphite()
    //report()
  }
  
  abstract class MetricsHandler {
    val registry = new MetricRegistry
    
    val totalConnections    = registry.counter("total_connections")
    val playerConnections   = registry.counter("player_connections")
    val offlinePlayers      = registry.counter("offlinePlayers")
    val connects            = registry.meter("connects")
    val disconnects         = registry.meter("disconnects")
    val messagesReceived    = registry.meter("messages_received")
    //val messagesBroadcasted = registry.meter("messages_broadcasted")
    //val messagesSent        = registry.meter("messages_sent")
    
    def connected(isPlayer: Boolean = false)
    def disconnected(isPlayer: Boolean = false)
    def messageReceived()
  }
  
  val metrics = new MetricsHandler {
    def connected(isPlayer: Boolean = false) {
      totalConnections.inc()
      if (isPlayer) playerConnections.inc()
      connects.mark()
    }
    
    def disconnected(isPlayer: Boolean = false) {
      totalConnections.dec()
      if (isPlayer) playerConnections.dec()
      disconnects.mark()
    }
    def messageReceived() {
      messagesReceived.mark()
    }
  }
  
}
