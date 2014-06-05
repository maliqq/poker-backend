package de.pokerno.gameplay.betting

import concurrent.duration._

import de.pokerno.gameplay.{Betting, Context, stg}

trait NextTurn {

  val ctx: stg.Context

  // Left(n) - pos n is next
  // Right(None) - stop deal, everyone folds except one
  // Right(Some(isShowdown)):
  // Right(Some(false)) - betting done, wait for action
  // Right(Some(true)) - betting done, go to showdown

  import ctx.gameplay._
  
  protected def nextTurn(): Either[Int, Option[Boolean]] = {
    round.seats filter (_._1 inPlay) foreach {
      case (seat, pos) ⇒
        if (!seat.didCall(round.call)) {
          //warn("not called, still playing: %s", seat)
          seat.play()
        }
    }

    if (round.seats.filter(_._1 inPot).size < 2) {
      return Right(None)
    }

    val playing = round.seats filter (_._1 isPlaying)
    if (playing.size == 0)
      Right(Some(round.seats.exists(_._1.isAllIn)))
    else Left(playing.head._2)
  }

}
