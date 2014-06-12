package de.pokerno.gameplay.betting

import concurrent.duration._

import de.pokerno.model.Seat
import de.pokerno.gameplay.{Betting, stg, Context => Gameplay}

trait NextTurn {

  val gameplay: Gameplay

  import gameplay._
  
  def nextTurn(): Betting.Transition = {
    round.seats.filter(_.inPlay) foreach { seat =>
      if (!seat.isCalled(round.callAmount)) seat.playing()
    }
    
    if (round.seats.filter(_.inPot).size < 2)
      Betting.Stop
    else {
      val playing = round.seats filter (_.isPlaying)
      
      if (playing.size > 1)
        Betting.Require(playing.head)
      else {
        if (playing.size - round.seats.count(_.isAllIn) <= 1)
          Betting.Showdown
        else
          Betting.Done
      }
    }
  }

}
