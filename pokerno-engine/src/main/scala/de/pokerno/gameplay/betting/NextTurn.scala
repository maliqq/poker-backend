package de.pokerno.gameplay.betting

import concurrent.duration._

import de.pokerno.gameplay.{Betting, Context, StageContext}

trait NextTurn {

  val ctx: StageContext
  
  protected def nextTurn(): Betting.Transition = {
    val round = ctx.gameplay.round
    
    round.seats filter (_._1 inPlay) foreach {
      case (seat, pos) ⇒
        if (!seat.didCall(round.call)) {
          //warn("not called, still playing: %s", seat)
          seat.play()
        }
    }

    if (round.seats.filter(_._1 inPot).size < 2) {
      return Betting.Stop
    }

    val playing = round.seats filter (_._1 isPlaying)
    if (playing.size == 0) {
      return if (round.seats.exists(_._1.isAllIn))  Betting.Showdown
             else                                   Betting.Done
    }

    Betting.Require(playing.head._2)
  }

}
