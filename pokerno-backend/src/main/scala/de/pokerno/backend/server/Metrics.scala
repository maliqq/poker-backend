package de.pokerno.backend.server

import akka.actor.{ Actor }
import com.codahale.metrics._
import de.pokerno.protocol.{msg => message}
import de.pokerno.gameplay.Notification
import java.util.concurrent.TimeUnit

object Metrics {
}

class Metrics extends Actor {
  final val metrics = new MetricRegistry

  val players = metrics.counter("players")
  
  val waiting = metrics.counter("waiting")
  val watchers = metrics.counter("watchers")
  
  val folds = metrics.counter("folds")
  val plays = metrics.meter("plays")
  val pots = metrics.histogram("pots")
  val playersPerFlop = metrics.meter("players-per-flop")
  
  override def preStart() {
    Console printf("metrics started!\n")
  }

  def receive = {
    case Notification(msg, _, _) ⇒ handleMessage(msg)
    case _ =>
  }
  
  override def postStop() {
  }
  
  var playersPreflop: Long = 0
  var lastPot: Double = .0
  
  import proto.wire.BetSchema.BetType
  import proto.wire.StreetType
  
  private def handleMessage(msg: message.Message) = msg match {
    case _: message.PlayerJoin =>
      players.inc()
      
    case _: message.PlayerLeave =>
      players.dec()
    
    case bet: message.AddBet =>
      handleBet(bet)
    
    case pot: message.DeclarePot =>
      lastPot = pot.pot
    
    case message.StreetStart(street) =>
      
      street match {
        case StreetType.PREFLOP =>
          
          playersPreflop = players.getCount()
          
        case StreetType.FLOP =>
          
          val playersCount = playersPreflop
          val foldsCount = folds.getCount()
          if (playersCount > 0) {
            val rate = (playersCount - foldsCount).toDouble / playersCount
            playersPerFlop.mark((rate * 100).floor.intValue)
          }
        
        case _ =>
      }

    case _: message.PlayStart =>
      plays.mark()
      
    case _: message.PlayStop =>
      // reset folds
      folds.dec(folds.getCount())
      
      // mark pot
      pots.update((lastPot * 100).intValue())
      lastPot = .0
      
      // report
      report()
    
    case _ =>
  }
  
  private def handleBet(bet: message.AddBet) = bet.bet.`type` match {
    case BetType.FOLD =>
      folds.inc()
      
    case _ =>
  }
  
  private def report() {
    Console printf("""~~~
                     | players: %d
                     | plays last 15m: %f
                     | avg pot=%.2f
                     | players per flop=%.2f%%
                     |~~~""".stripMargin, players.getCount(),
        plays.getFifteenMinuteRate() * 3600,
        pots.getSnapshot().getMedian() / 100.0,
        playersPerFlop.getMeanRate())
  }
}
