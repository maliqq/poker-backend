package de.pokerno.gameplay.betting

import concurrent.duration._

import de.pokerno.model.Seat
import de.pokerno.gameplay.{Betting, stg, Context => Gameplay}

trait NextTurn {

  val gameplay: Gameplay

  // Left(n) - pos n is next
  // Right(None) - stop deal, everyone folds except one
  // Right(Some(isShowdown)):
  // Right(Some(false)) - betting done, wait for action
  // Right(Some(true)) - betting done, go to showdown

  import gameplay._
  
  protected def nextTurn(): Either[Seat, Option[Boolean]] = {
    round.seats filter (_.inPlay) foreach { seat =>
      if (!seat.didCall(round.callAmount)) {
        //warn("not called, still playing: %s", seat)
        seat.play()
      }
    }

    if (round.seats.filter(_.inPot).size < 2) {
      return Right(None)
    }

    val playing = round.seats filter (_.isPlaying)
    
    if (playing.size > 0)  Left(playing.head)
    else                   Right(Some(round.seats.exists(_.isAllIn)))
  }

}
