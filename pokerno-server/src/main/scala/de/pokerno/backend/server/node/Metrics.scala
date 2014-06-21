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
        totalConnections.getCount(),
        playerConnections.getCount(),
        offlinePlayers.getCount(),
        thrift.metrics.Meter(mean = connects.getMeanRate(), rate15 = Some(connects.getFifteenMinuteRate())),
        thrift.metrics.Meter(mean = disconnects.getMeanRate(), rate15 = Some(disconnects.getFifteenMinuteRate())),
        thrift.metrics.Meter(mean = messagesReceived.getMeanRate(), rate15 = Some(messagesReceived.getFifteenMinuteRate()))
    )
    pokerdb match {
      case Some(service) => service.reportNodeMetrics(nodeId, m)
      case _ => println(m)
    }
  }
}

class Metrics(val nodeId: String, val pokerdb: Option[thrift.PokerDB.FutureIface]) extends Actor with ActorLogging with Reporting {
  import context._
  
  val metrics = new MetricRegistry
  
  val totalConnections    = metrics.counter("total_connections")
  val playerConnections   = metrics.counter("player_connections")
  val offlinePlayers      = metrics.counter("offlinePlayers")
  val connects            = metrics.meter("connects")
  val disconnects         = metrics.meter("disconnects")
  val messagesReceived    = metrics.meter("messages_received")
  //val messagesBroadcasted = metrics.meter("messages_broadcasted")
  //val messagesSent        = metrics.meter("messages_sent")
  
  override def preStart() {
    system.scheduler.schedule(1.minute, 1.minute) {
      report()
    }
    //report()
  }
  
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
