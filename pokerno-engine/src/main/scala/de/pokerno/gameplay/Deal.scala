package de.pokerno.gameplay

import akka.actor.{ Actor, Props, ActorRef, ActorLogging }
import de.pokerno.model._
import de.pokerno.poker.Card
import concurrent._
import concurrent.duration._
import de.pokerno.gameplay.betting.NextTurn

object Deal {

  case object Start
  case object Cancel
  case object Stop

  case class Next(after: FiniteDuration = 5 seconds)
  case class Done //(after: FiniteDuration)

  // for archiver
  case class Dump(
    id:     java.util.UUID,
    game:   Game,
    stake:  Stake,
    play:   Play
  )
  def dump(id: java.util.UUID, ctx: Context) = Dump(id, ctx.game, ctx.stake, ctx.play)
}

class Deal(val gameplay: Context) extends Actor
    with ActorLogging
    with Stages.Default {
  
  import context._
  
  val gameplayContext = new Stage.Context(gameplay, self)
  
  var roundContext: round.Context = null
  val bettingContext = new Betting.Context(gameplay, self)
  val discardingContext = new Discarding.Context(gameplay, self)
  
  lazy private val onStreets = Streets(gameplayContext)
  
  override def preStart() {
    beforeStreets.apply(gameplayContext) match {
      case Stage.Next ⇒
        self ! Streets.Next
      case Stage.Exit ⇒
        log.warning("[stage] exit")
        cancel()
    }
  }

  override def postStop() {
  }

  def receive = receiveStreets

  def receiveStreets: Receive = {
    // rounds control
    case Betting.Start ⇒
      log.info("[betting] start")
      roundContext = bettingContext
      context.become(receiveRound orElse receiveBets)
      self ! roundContext.nextTurn()
    
    case Discarding.Start =>
      log.info("[discarding] start")
      roundContext = discardingContext
      context.become(receiveRound orElse receiveDiscards)
      self ! roundContext.nextTurn()

    // streets flow control
    case Streets.Next ⇒
      log.info("[streets] next")
      onStreets.next()

    case Streets.Continue =>
      log.info("[streets] continue")
      onStreets.continue()
      
    case Streets.Done ⇒
      log.info("[streets] done")
      afterStreets.apply(gameplayContext)
      done()
  }
  
  def receiveRound: Receive = {
    case Round.Require(seat) ⇒
      roundContext.require(seat)
      seat.actingTimer.start(new AkkaTimers(system.scheduler, system.dispatcher)) {
        self ! Round.Timeout
      }
    
    case Round.Timeout ⇒
      log.info("[round] timeout")
      roundContext.timeout()
      
    case Round.Stop ⇒
      log.info("[round] stop")
      roundContext.complete()
      context.become(receiveStreets)
      self ! Streets.Done

    case Round.Done ⇒
      log.info("[betting] done")
      roundContext.complete()
      context.become(receiveStreets)
      onStreets.continue()
  }
  
  def receiveBets: Receive = {
    case Betting.Add(player, bet) ⇒
      bettingContext.add(player, bet)
    
    case Betting.Cancel(player) =>
      bettingContext.cancel(player)

    case Betting.Showdown ⇒
      log.warning("[betting] showdown")
      bettingContext.complete()
      context.become(receiveStreets)
      //self ! Streets.Next
      system.scheduler.scheduleOnce(1.second, self, Streets.Next)

    case Betting.BigBets ⇒
      log.info("[betting] big bets")
      bettingContext.bigBets()
  }
  
  def receiveDiscards: Receive = {
    case Discarding.Discard(player, cards) =>
      discardingContext.discard(player, cards)
  }

  private def cancel() {
    parent ! Deal.Cancel
    context stop self
  }

  private def done() {
    val showdownHandsNum = gameplay.play.knownCards.size
    parent ! Deal.Done
    context stop self
  }

}
