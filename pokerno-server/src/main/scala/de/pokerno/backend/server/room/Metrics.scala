package de.pokerno.backend.server

import akka.actor.{ Actor, ActorLogging }
import com.codahale.metrics._
import java.util.concurrent.TimeUnit

import de.pokerno.protocol.{msg => message}
import de.pokerno.model.{Bet, Street}
import de.pokerno.gameplay.Notification

object Metrics {
}

class Metrics(id: String) extends Actor with ActorLogging {
  final val metrics = new MetricRegistry
  //val reporter = ConsoleReporter.forRegistry(metrics).build()

  val players         = metrics.counter("players")

  val waiting         = metrics.counter("waiting")
  val watchers        = metrics.counter("watchers")

  val folds           = metrics.counter("folds")
  val plays           = metrics.meter("plays")
  val pots            = metrics.histogram("pots")
  val playersPerFlop  = metrics.meter("players-per-flop")

  override def preStart() {
  }

  def receive = {
    case Notification(msg, _, _) ⇒ handleMessage(msg)
    case _                       ⇒
  }

  override def postStop() {
  }

  var playersPreflop: Long = 0
  var lastPot: Double = .0

  private def handleMessage(msg: message.GameEvent) = msg match {
    case _: message.PlayerJoin ⇒
      players.inc()

    case _: message.PlayerLeave ⇒
      players.dec()

    case bet: message.DeclareBet ⇒
      handleBet(bet)

    case message.DeclarePot(pot, _) ⇒
      lastPot = pot.total.toDouble

    case message.DeclareStreet(street) ⇒

      street match {
        case Street.Preflop ⇒

          playersPreflop = players.getCount()

        case Street.Flop ⇒

          val playersCount = playersPreflop
          val foldsCount = folds.getCount()
          if (playersCount > 0) {
            val rate = (playersCount - foldsCount).toDouble / playersCount
            playersPerFlop.mark((rate * 100).floor.intValue)
          }

        case _ ⇒
      }

    case _: message.DeclarePlayStart ⇒
      plays.mark()

    case _: message.DeclarePlayStop ⇒
      // reset folds
      folds.dec(folds.getCount())

      // mark pot
      pots.update((lastPot * 100).intValue())
      lastPot = .0

      // report
      report()

    case _ ⇒
  }

  private def handleBet(bet: message.DeclareBet) = bet.action match {
    case Bet.Fold ⇒
      folds.inc()

    case _ ⇒
  }

  private def report() {
    Console printf ("[metrics] room %s stats: players: %d plays/hour: %f avg pot=%.2f players per flop=%.2f%%\n", id, 
      players.getCount(),
      plays.getFifteenMinuteRate() * 3600,
      pots.getSnapshot().getMedian() / 100.0,
      playersPerFlop.getMeanRate())
  }
  
}
