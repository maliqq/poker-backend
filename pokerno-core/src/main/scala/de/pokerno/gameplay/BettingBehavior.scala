package de.pokerno.gameplay

import akka.actor.Actor

trait BettingBehavior {
  
  a: DealActor =>

  def handleBetting: Receive = {
    case Betting.Add(bet) ⇒
      gameplay.round.addBet(bet)
      nextTurn

    case Betting.Start ⇒
      gameplay.round.reset
      nextTurn

    case Betting.Next ⇒
      nextTurn

    case Betting.Stop ⇒
      
    case Betting.Timeout ⇒
      nextTurn

    case Betting.Done ⇒
      gameplay.round.complete
      streets(stageContext)

    case Betting.BigBets ⇒
      gameplay.round.bigBets = true
  }
  
  private def nextTurn {
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
