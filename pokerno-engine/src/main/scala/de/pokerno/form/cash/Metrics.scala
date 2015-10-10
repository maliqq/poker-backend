package de.pokerno.form.cash

import akka.actor.{ Actor, ActorLogging }
import com.codahale.metrics._
import concurrent.duration._
import java.util.concurrent.TimeUnit
import de.pokerno.data.pokerdb.thrift
import com.fasterxml.jackson.annotation.JsonGetter

import de.pokerno.protocol.{msg => message}
import de.pokerno.model.{Bet, Street}
import de.pokerno.gameplay.Notification

trait PlayerMetrics {
  val registry: MetricRegistry

  lazy val players         = registry.counter("players")
  lazy val waiting         = registry.counter("waiting")
  lazy val watching        = registry.counter("watchers")
  
  @JsonGetter("players_count")    def playersCount = players.getCount()
  @JsonGetter("waiting_count")    def waitingCount = waiting.getCount()
  @JsonGetter("watching_count")   def watchingCount = watching.getCount()
}

trait PlayStats {
  val registry: MetricRegistry

  lazy val folds           = registry.counter("folds")
  lazy val pots            = registry.histogram("pots")
  lazy val plays           = registry.meter("plays")
  lazy val playersPerFlop  = registry.meter("players-per-flop")

  @JsonGetter("plays_rate")       def playsRate = plays.getFifteenMinuteRate()
  @JsonGetter("average_pot")      def averagePot = pots.getSnapshot().getMean() / 100.0
  @JsonGetter("players_per_flop") def playersPerFlopMean = playersPerFlop.getMeanRate()
}

sealed class Metrics extends PlayerMetrics with PlayStats {
  final val registry = new MetricRegistry
}

trait Reporting {
  def reportPlayersCountUpdate()
  def reportPlayStatsUpdate()
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
      reportPlayersCountUpdate()

    case _: message.PlayerLeave ⇒
      metrics.players.dec()
      reportPlayersCountUpdate()

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
      reportPlayStatsUpdate()

    case _ ⇒
  }

  private def handleBet(bet: message.DeclareBet) = bet.action match {
    case Bet.Fold ⇒
      metrics.folds.inc()

    case _ ⇒
  }

}
