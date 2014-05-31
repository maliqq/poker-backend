package de.pokerno.gameplay.betting

import concurrent.duration._
import de.pokerno.gameplay.{Betting, Context, StageContext}

trait NextTurn {

  def gameplay: Context
  def stageContext: StageContext

  protected def nextTurn(): Betting.Transition = {
    val round = gameplay.round

    Console printf ("%s%s%s\n", Console.MAGENTA, gameplay.table, Console.RESET)

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
