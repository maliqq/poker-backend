package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import java.util.Locale
import util.control.Breaks._
import com.fasterxml.jackson.annotation.{ JsonValue, JsonIgnore, JsonProperty }

class SidePot(val capFrom: Decimal = 0, val cap: Option[Decimal] = None) {
  // members
  var members: collection.mutable.Map[Player, Decimal] = collection.mutable.Map.empty
  
  def isMember(member: Player)  = members.contains(member)
  @JsonValue def total: Decimal = members.values.sum
  def isActive: Boolean         = members.size > 0 && total > .0

  def add(member: Player, left: Decimal): Decimal = add(member, left, left)

  def add(member: Player, _amount: Decimal, left: Decimal): Decimal = {
    if (left == 0) return 0

    val value: Decimal = members.getOrElse(member, 0)
    val amount = value + _amount
    val newValue = value + left

    if (!cap.isDefined) {
      members(member) = newValue
      return 0
    }

    val capAmount = cap.get
    if (amount >= capFrom && capAmount <= amount) { // (capAmount > value) {
      members(member) = capAmount
      return newValue - capAmount
    }

    left
  }

  def split(member: Player, left: Decimal): Tuple2[SidePot, SidePot] = split(member, left, left)

  def split(member: Player, _amount: Decimal, left: Decimal, cap: Option[Decimal] = None): Tuple2[SidePot, SidePot] = {
    val value: Decimal = members getOrElse (member, 0)
    val bet = value + left
    val amount = value + _amount

    members(member) = bet

    val _new = new SidePot(amount, cap map { capAmount ⇒
      capAmount - bet
    } orElse (Some(amount)))

    _new.members = members
      .filter { case (key, _value) ⇒ _value > bet && key != member }
      .map { case (key, _value) ⇒ (key, _value - bet) }

    val _old = new SidePot(capFrom, Some(bet))

    _old.members = members map {
      case (key, _value) ⇒
        (key, List(_value, bet).min)
    }

    (_new, _old)
  }

  override def toString = {
    val s = new StringBuilder

    s.append(
      members.map {
        case (member, amount) ⇒
          "* %s:\t%.2f" formatLocal (Locale.US, member, amount)
      }.mkString("\n")
    )
    s.append(" = %.2f" formatLocal (Locale.US, total))
    //if (cap.isDefined)
    s.append(" (cap: %.2f+%.2f)" formatLocal (Locale.US, capFrom, cap.getOrElse(.0)))

    s.append("\n")
    s.toString()
  }
}

class Pot {
  @JsonIgnore var main: SidePot = new SidePot
  private var _side: List[SidePot] = List.empty
  private var inactive: List[SidePot] = List.empty

  def side = _side
  
  @JsonProperty def total: Decimal = sidePots.map(_.total).sum

  import collection.mutable.ListBuffer
  
  @JsonProperty("side") def sidePots: List[SidePot] = {
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
    inactive ++= side ++ List(main)
    main = new SidePot
    _side = List.empty
  }

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
