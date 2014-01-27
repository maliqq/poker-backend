package de.pokerno.gameplay

import akka.actor.Actor

trait BettingBehavior {
  
  a: DealActor =>

  def handleBetting: Receive = {
    case Betting.Add(bet) ⇒
      log.info("[betting] add {}", bet)
      gameplay.round.addBet(bet)
      nextTurn

    case Betting.NextTurn ⇒
      log.info("[betting] next turn")
      nextTurn

    case Betting.Stop ⇒
      log.info("[betting] stop")
      context.become(handleStreets)
      self ! Streets.Done
      
    case Betting.Timeout ⇒
      log.info("[betting] timeout")
      nextTurn

    case Betting.Done ⇒
      log.info("[betting] done")
      gameplay.round.complete
      context.become(handleStreets)
      streets(stageContext)

    case Betting.BigBets ⇒
      log.info("[betting] big bets")
      gameplay.round.bigBets = true
  }
  
  protected def nextTurn {
    gameplay.round.move
    gameplay.round.seats filter (_._1 inPlay) foreach {
      case (seat, pos) ⇒
        if (!seat.isCalled(gameplay.round.call)) seat playing
    }

    if (gameplay.round.seats.filter(_._1 inPot).size < 2)
      self ! Betting.Stop
    else {
      val active = gameplay.round.seats filter (_._1 isPlaying)

      if (active.size == 0)
        self ! Betting.Done
      else
        gameplay.round requireBet (active.head)
    }
  }

}
