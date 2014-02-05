package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import collection.mutable

class SidePot(val cap: Option[Decimal] = None) {
  var members: Map[Player, Decimal] = Map.empty
  
  import de.pokerno.util.ConsoleUtils._

  def total: Decimal = members.values.sum

  def isActive: Boolean = members.size > 0 && total > .0

  def add(member: Player, amount: Decimal): Decimal = {
    if (amount.toDouble == .0) return .0

    val value: Decimal = members.getOrElse(member, .0)
    val newValue = value + amount

    if (!cap.isDefined) {
      members += (member -> newValue)
      return .0
    }
    
    val capAmount = cap.get
    if (capAmount > value) {
      warn("player %s capAmount (%s) > value(%s)", member, capAmount, value)
      members += (member -> capAmount)
      return newValue - capAmount
    }
    
    amount
  }

  def split(member: Player, left: Decimal): Tuple2[SidePot, SidePot] = {
    val value: Decimal = members getOrElse (member, .0)
    val bet = value + left
    members += (member -> bet)

    val _new = new SidePot
    _new.members = members.
      filter { case (key, _value) ⇒ _value > bet && key != member }.
      map { case (key, _value) ⇒ (key, _value - bet) }

    val _old = new SidePot(Some(bet))
    _old.members = members map {
      case (key, _value) ⇒
        (key, List(_value, bet).min)
    }
    
    warn("_old=%s _new=%s", _old, _new)
    
    (_new, _old)
  }

  override def toString = {
    val s = new StringBuilder
    members foreach {
      case (member, amount) ⇒
        s ++= "%s: %.2f\n" format (member, amount)
    }
    s ++= "-- Total: %.2f" format total
    if (cap.isDefined) s ++= " Cap: %.2f" format (cap get)
    s.toString()
  }
}

class Pot {
  var current: SidePot = new SidePot
  var active: List[SidePot] = List.empty
  var inactive: List[SidePot] = List.empty

  def total: Decimal = sidePots.map(_.total).sum

  def sidePots: List[SidePot] = {
    var pots = new mutable.ListBuffer[SidePot]
    if (current.isActive)
      pots += current
    (active ++ inactive) foreach { side ⇒
      if (side.isActive)
        pots += side
    }
    pots.toList
  }

  def split(member: Player, amount: Decimal) {
    val (_new, _old) = current split (member, amount)
    active :+= _old
    current = _new
  }
  
  def complete() {
    inactive ++= active ++ List(current)
    current = new SidePot
    active = List.empty
  }

  def add(member: Player, amount: Decimal) =
    active.foldRight[Decimal](amount) {
      case (sidePot, left) ⇒
        sidePot add (member, left)
    }
//    active.foldLeft[Decimal](amount) {
//      case (left, sidePot) ⇒
//        sidePot add (member, left)
//    }
  
  override def toString = {
    val s = new StringBuilder
    
    s.append(current.toString)
    sidePots foreach { sidePot =>
      s.append(sidePot.toString + "\n")
    }
    
    s.toString()
  }
}
