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
}

trait Reporting { m: Metrics =>
  def report() {
    val m = thrift.metrics.Room(
        players.getCount(),
        waiting.getCount(),
        watching.getCount(),
        thrift.metrics.Histogram(mean = pots.getSnapshot().getMean() / 100.0), // 100 - for floats
        thrift.metrics.Meter(mean = plays.getMeanRate(), rate15 = Some(plays.getFifteenMinuteRate())),
        thrift.metrics.Meter(mean = playersPerFlop.getMeanRate(), rate15 = Some(playersPerFlop.getFifteenMinuteRate()))
    )
    pokerdb match {
      case Some(service) => service.reportRoomMetrics(roomId, m)
      case _ => println(m)
    }
  }
}

// TODO reporters
class Metrics(val roomId: String, val pokerdb: Option[thrift.PokerDB.FutureIface]) extends Actor with ActorLogging with Reporting {
  import context._

  final val metrics = new MetricRegistry
  //val reporter = ConsoleReporter.forRegistry(metrics).build()

  val players         = metrics.counter("players")

  val waiting         = metrics.counter("waiting")
  val watching        = metrics.counter("watchers")

  private val _folds  = metrics.counter("folds")
  val pots            = metrics.histogram("pots")
  val plays           = metrics.meter("plays")
  val playersPerFlop  = metrics.meter("players-per-flop")

  override def preStart() {
  }

  def receive = {
    case Notification(msg, _, _, _) ⇒ handleMessage(msg)
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
          val foldsCount = _folds.getCount()
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
      _folds.dec(_folds.getCount())

      // mark pot
      pots.update((lastPot * 100).intValue())
      lastPot = .0

      // report
      report()

    case _ ⇒
  }

  private def handleBet(bet: message.DeclareBet) = bet.action match {
    case Bet.Fold ⇒
      _folds.inc()

    case _ ⇒
  }

}
