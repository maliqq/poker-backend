package de.pokerno.model

import util.control.Breaks._
import com.fasterxml.jackson.annotation.{ JsonValue, JsonIgnore, JsonProperty, JsonPropertyOrder }

@JsonPropertyOrder(Array("total", "side"))
class Pot {
  @JsonIgnore var main: SidePot = new SidePot
  
  private var _side: List[SidePot] = List.empty
  @JsonProperty def side = _side
  
  private var inactive: List[SidePot] = List.empty
  
  @JsonProperty def total: Decimal = _side.map(_.total).sum

  import collection.mutable.ListBuffer
  
  def sidePots: List[SidePot] = {
    var pots = new ListBuffer[SidePot]
    if (main.isActive)
      pots += main
    (side ++ inactive) foreach { side ⇒
      if (side.isActive)
        pots += side
    }
    pots.toList
  }

  def split(member: Player, _amount: Decimal, left: Decimal) {
    val value: Decimal = main.members.getOrElse(member, 0)
    val amount = value + _amount
    if (main.capFrom <= amount) {
      //Console printf("splitting main: %s for member %s with amount %s/%s", main, member, amount, left)
      val (_new, _old) = main split (member, _amount, left)
      _side :+= _old
      main = _new
    } else {
      var (skip, newSide) = side.span { sidePot ⇒
        sidePot.capFrom <= amount
      }

      val current = if (!skip.isEmpty) {
        val _current = skip.last
        skip = skip.dropRight(1)
        _current
      } else {
        val _current = newSide.head
        newSide = newSide.drop(1)
        _current
      }

      //Console printf("splitting side: %s for member %s with amount %s/%s and current cap", current, member, amount, left)
      val (_new, _old) = current split (member, _amount, left, current.cap)
      _side = skip ++ List(_old, _new) ++ newSide
    }
    //Console printf("main=%s\nside=%s", main, side)
  }
  
  def complete() {
    if (main.isActive) _side ++= List(main)
    main = new SidePot
  }

//  def complete() {
//    inactive ++= side ++ List(main)
//    main = new SidePot
//    _side = List.empty
//  }

  private def allocate(member: Player, amount: Decimal) =
    side.foldLeft[Decimal](amount) {
      case (left, sidePot) ⇒
        sidePot add (member, amount, left)
    }

  def add(member: Player, amount: Decimal, isAllIn: Boolean = false): Decimal = {
    val left = allocate (member, amount)

    if (isAllIn && left != 0) {
      split (member, amount, left)
      0
    } else main add (member, amount, left)
  }

  override def toString = {
    val s = new StringBuilder

    s.append(main.toString)
    sidePots foreach { sidePot ⇒
      s.append(sidePot.toString + "\n")
    }

    s.toString()
  }
}
