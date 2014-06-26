package de.pokerno.model

import util.control.Breaks._
import com.fasterxml.jackson.annotation.{ JsonValue, JsonIgnore, JsonProperty, JsonPropertyOrder }

@JsonPropertyOrder(Array("total", "side"))
class Pot {
  @JsonIgnore var main: SidePot = new SidePot
  
  private var _side: List[SidePot] = List.empty
  @JsonProperty def side = _side
  
  private var inactive: List[SidePot] = List.empty
  
  @JsonProperty def total: Decimal = main.total + _side.map(_.total).sum

  import collection.mutable.ListBuffer
  
  def sidePots: List[SidePot] = {
    var pots = new ListBuffer[SidePot]
    if (!main.isEmpty)
      pots += main
    (side ++ inactive) foreach { side ⇒
      if (!side.isEmpty)
        pots += side
    }
    pots.toList
  }

  //def split(player: Player, uncalled: Decimal) {
//    val value = main.members(player)
//    val amount = value + diff
//    Console printf("----------------\nmain=%s\n", main)
//    //if (amount >= main.call) {
//      Console printf("splitting main: %s for player %s with amount: %s uncalled: %s\n", main, player, amount, uncalled)
//      val (_new, _old) = main split (player, diff, uncalled)
//      _side = _old :: _side
//      main = _new
//    } else {
//      var (newSide, skip) = side.span { sidePot ⇒
//        sidePot.call <= amount
//      }
//      Console printf("newSide=%s skip=%s\n", newSide, skip)
//
//      val current = if (skip.isEmpty) {
//        val _current = newSide.head
//        newSide = newSide.drop(1)
//        _current
//      } else {
//        val _current = skip.last
//        skip = skip.dropRight(1)
//        _current
//      }
//      Console printf("splitting side: %s for player %s with amount: %s uncalled: %s and current cap: %s\n", current, player, amount, uncalled, current.cap)
//      
//      val (_new, _old) = current split (player, diff, uncalled, current.cap)
//      _side = newSide ++ List(_old, _new) ++ skip
//    }
    //Console printf("main=%s\nside=%s", main, side)
  //}
  
  def complete() {
    if (!main.isEmpty) _side = main :: _side
    main = new SidePot
  }

//  def complete() {
//    inactive ++= side ++ List(main)
//    main = new SidePot
//    _side = List.empty
//  }

  private def advance(player: Player, diff: Decimal) =
    _side.foldLeft[Decimal](diff) { case (uncalled, sidePot) ⇒
      sidePot add (player, uncalled)
    }

  def add(player: Player, diff: Decimal, isAllIn: Boolean = false): Decimal = {
    var uncalled = advance (player, diff)
    if (isAllIn && uncalled != 0) {
      main.split(player, uncalled) match {
        case Some(side) => _side = side :: _side
        case None =>
      }
      return 0
    }
    if (main.cap.isDefined && uncalled != 0) {
      uncalled = main add (player, uncalled)
      if (uncalled == 0) return 0
      _side = main :: _side
      main = new SidePot
    }
    main add (player, uncalled)
  }

  override def toString = {
    val s = new StringBuilder

    s.append("MAIN:\n" + main.toString + "\nSIDE:\n")
    _side foreach { sidePot ⇒
      s.append(sidePot.toString + "\n")
    }

    s.toString()
  }
}
