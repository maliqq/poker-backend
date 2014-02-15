package de.pokerno.protocol.msg

import de.pokerno.gameplay
import de.pokerno.protocol.wire
import de.pokerno.protocol.wire.Conversions._
import de.pokerno.protocol.Conversions._
import proto.wire.StreetType

object Conversions {

  implicit def street2wire(s: gameplay.Street.Value): StreetType = s match {
    case gameplay.Street.Preflop    ⇒ StreetType.PREFLOP
    case gameplay.Street.Flop       ⇒ StreetType.FLOP
    case gameplay.Street.Turn       ⇒ StreetType.TURN
    case gameplay.Street.River      ⇒ StreetType.RIVER

    case gameplay.Street.Third      ⇒ StreetType.THIRD
    case gameplay.Street.Fourth     ⇒ StreetType.FOURTH
    case gameplay.Street.Fifth      ⇒ StreetType.FIFTH
    case gameplay.Street.Sixth      ⇒ StreetType.SIXTH
    case gameplay.Street.Seventh    ⇒ StreetType.SEVENTH

    case gameplay.Street.Predraw    ⇒ StreetType.PREDRAW
    case gameplay.Street.Draw       ⇒ StreetType.DRAW
    case gameplay.Street.FirstDraw  ⇒ StreetType.FIRST_DRAW
    case gameplay.Street.SecondDraw ⇒ StreetType.SECOND_DRAW
    case gameplay.Street.ThirdDraw  ⇒ StreetType.THIRD_DRAW
  }

  implicit def play2wire(v: gameplay.Play): Play = {
    if (v != null) {
      val play = new Play(v.id, v.startAt.getTime() / 1000)
      play.pot = v.pot.total
      play.street = v.street
      play.acting = RequireBet(pos = v.acting._2, player = v.acting._1, call = v.require._1, raise = v.require._2)
      play
    } else null
  }

}
