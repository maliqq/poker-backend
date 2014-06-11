package de.pokerno.gameplay.betting

import concurrent.duration._

import de.pokerno.model.Seat
import de.pokerno.gameplay.{Betting, stg, Context => Gameplay}

trait NextTurn {

  val gameplay: Gameplay

  import gameplay._
  
  def nextTurn(): Betting.Transition = {
    round.seats filter (_.inPlay) foreach { seat =>
      if (!seat.didCall(round.callAmount)) {
        //warn("not called, still playing: %s", seat)
        seat.play()
      }
    }

    if (round.seats.filter(_.inPot).size < 2)
      Betting.Stop
    else {
      val playing = round.seats filter (_.isPlaying)
      
      if (playing.size > 0)
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
