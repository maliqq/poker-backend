package de.pokerno.protocol.msg

import de.pokerno.gameplay
import de.pokerno.protocol.wire
import de.pokerno.protocol.Conversions._

object Conversions {

  implicit def street2wire(s: gameplay.Street.Value): wire.StreetType = s match {
    case gameplay.Street.Preflop    ⇒ wire.StreetType.PREFLOP
    case gameplay.Street.Flop       ⇒ wire.StreetType.FLOP
    case gameplay.Street.Turn       ⇒ wire.StreetType.TURN
    case gameplay.Street.River      ⇒ wire.StreetType.RIVER

    case gameplay.Street.Third      ⇒ wire.StreetType.THIRD
    case gameplay.Street.Fourth     ⇒ wire.StreetType.FOURTH
    case gameplay.Street.Fifth      ⇒ wire.StreetType.FIFTH
    case gameplay.Street.Sixth      ⇒ wire.StreetType.SIXTH
    case gameplay.Street.Seventh    ⇒ wire.StreetType.SEVENTH

    case gameplay.Street.Predraw    ⇒ wire.StreetType.PREDRAW
    case gameplay.Street.Draw       ⇒ wire.StreetType.DRAW
    case gameplay.Street.FirstDraw  ⇒ wire.StreetType.FIRST_DRAW
    case gameplay.Street.SecondDraw ⇒ wire.StreetType.SECOND_DRAW
    case gameplay.Street.ThirdDraw  ⇒ wire.StreetType.THIRD_DRAW
  }

  implicit def play2wire(v: gameplay.Play): Play = {
    val play = new Play(v.id, v.startAt.getTime() / 1000)
    play.pot = v.pot.total
    play.street = v.street
    play
  }

}
