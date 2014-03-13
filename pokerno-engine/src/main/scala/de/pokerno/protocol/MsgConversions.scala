package de.pokerno.protocol

import math.{ BigDecimal ⇒ Decimal }
import de.pokerno.{ model, poker, gameplay }

import com.dyuproject.protostuff.ByteString

object MsgConversions {

  import CommonConversions._
  import de.pokerno.protocol.msg._
  import proto.wire.StreetType

  implicit def street2wire(s: gameplay.Street.Value): StreetType = s match {
    //case null => null
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
      v.street.map { street ⇒
        play.street = street
      }
      v.acting.map { a ⇒
        play.acting = RequireBet(pos = a._2, player = a._1, call = v.require._1, raise = v.require._2)
      }
      if (v.board != null) play.board = v.board
      //if (v.pocket != null) play.pocket = v.pocket
      play
    } else null
  }

}
