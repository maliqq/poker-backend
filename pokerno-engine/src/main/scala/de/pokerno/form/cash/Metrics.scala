package de.pokerno.form.cash

import akka.actor.{ Actor, ActorLogging }
import com.codahale.metrics._
import concurrent.duration._
import java.util.concurrent.TimeUnit
import de.pokerno.data.pokerdb.thrift

import de.pokerno.protocol.{msg => message}
import de.pokerno.model.{Bet, Street}
import de.pokerno.gameplay.Notification

object Metrics {
  implicit def metrics2thrift(m: Metrics): thrift.metrics.Room =
    thrift.metrics.Room(
      m.players.getCount(),
      m.waiting.getCount(),
      m.watching.getCount(),
      thrift.metrics.Histogram(mean = m.pots.getSnapshot().getMean() / 100.0), // 100 - for floats
      thrift.metrics.Meter(mean = m.plays.getMeanRate(), rate15 = Some(m.plays.getFifteenMinuteRate())),
      thrift.metrics.Meter(mean = m.playersPerFlop.getMeanRate(), rate15 = Some(m.playersPerFlop.getFifteenMinuteRate()))
    )
}

sealed class Metrics {
  final val registry = new MetricRegistry

  val players         = registry.counter("players")
  val waiting         = registry.counter("waiting")
  val watching        = registry.counter("watchers")

  val folds           = registry.counter("folds")

  val pots            = registry.histogram("pots")
  val plays           = registry.meter("plays")
  val playersPerFlop  = registry.meter("players-per-flop")

  import com.fasterxml.jackson.annotation.JsonGetter
  @JsonGetter("players_count")    def playersCount = players.getCount()
  @JsonGetter("waiting_count")    def waitingCount = waiting.getCount()
  @JsonGetter("watching_count")   def watchingCount = watching.getCount()
  @JsonGetter("plays_rate")       def playsRate = plays.getFifteenMinuteRate()
  @JsonGetter("average_pot")      def averagePot = pots.getSnapshot().getMean() / 100.0
  @JsonGetter("players_per_flop") def playersPerFlopMean = playersPerFlop.getMeanRate()
}

trait Reporting {
  def report()
}

// TODO reporters
abstract class MetricsCollector extends Actor with ActorLogging with Reporting {
  import context._

  //val reporter = ConsoleReporter.forRegistry(metrics).build()

  val metrics = new Metrics()

  def receive = {
    case Notification(msg, _, _, _) ⇒ handleMessage(msg)
    case _                       ⇒
  }

  var playersPreflop: Long = 0
  var lastPot: Double = .0

  private def handleMessage(msg: message.GameEvent) = msg match {
    case _: message.PlayerJoin ⇒
      metrics.players.inc()

    case _: message.PlayerLeave ⇒
      metrics.players.dec()

    case bet: message.DeclareBet ⇒
      handleBet(bet)

    case message.DeclarePot(pot, _) ⇒
      lastPot = pot.total.toDouble

    case message.DeclareStreet(Street.Preflop) ⇒
      playersPreflop = metrics.players.getCount()

    case message.DeclareStreet(Street.Flop) ⇒
      val playersCount = playersPreflop
      val foldsCount = metrics.folds.getCount()
      if (playersCount > 0) {
        val rate = (playersCount - foldsCount).toDouble / playersCount
        metrics.playersPerFlop.mark((rate * 100).floor.intValue)
      }

    case _: message.DeclarePlayStart ⇒
      metrics.plays.mark()

    case _: message.DeclarePlayStop ⇒
      // reset folds
      metrics.folds.dec(metrics.folds.getCount())

      // mark pot
      metrics.pots.update((lastPot * 100).intValue())
      lastPot = .0

      // report
      report()

    case _ ⇒
  }

  private def handleBet(bet: message.DeclareBet) = bet.action match {
    case Bet.Fold ⇒
      metrics.folds.inc()

    case _ ⇒
  }

}
