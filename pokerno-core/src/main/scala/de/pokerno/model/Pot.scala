package de.pokerno.model

import math.{ BigDecimal ⇒ Decimal }
import collection.mutable
import java.util.Locale

class SidePot(val cap: Option[Decimal] = None) {
  var members: Map[Player, Decimal] = Map.empty
  
  def isMember(member: Player) = members.contains(member)

  import de.pokerno.util.ConsoleUtils._
  
  info("| --- side pot with cap %s created", cap)

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
    _new.members = members
      .filter { case (key, _value) ⇒ _value > bet && key != member }
      .map { case (key, _value) ⇒ (key, _value - bet) }

    val _old = new SidePot(Some(bet))
    _old.members = members map {
      case (key, _value) ⇒
        (key, List(_value, bet).min)
    }
    
    warn("_old: %s\n_new: %s", _old, _new)
    
    (_new, _old)
  }

  override def toString = {
    val s = new StringBuilder
    
    s.append(
        members.map { case (member, amount) ⇒
          "%.2f (%s)" formatLocal (Locale.US, amount, member)
        }.mkString(" + ")
      )
    s.append(" = %.2f" formatLocal (Locale.US, total))
    if (cap.isDefined)
      s.append(" (cap: %.2f)" formatLocal (Locale.US, cap.get))
    
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

  import de.pokerno.util.ConsoleUtils._

  def split(member: Player, amount: Decimal) {
    info("splitting current=%s for member %s with amount %s", current, member, amount)
    val (_new, _old) = current split (member, amount)
    
    active :+= _old
    current = _new
  }
  
  def complete() {
    inactive ++= active ++ List(current)
    current = new SidePot
    active = List.empty
  }
  
  private def allocate(member: Player, amount: Decimal) = {
    active.foldRight[Decimal](amount) {
      case (sidePot, left) ⇒
        sidePot add (member, left)
    }
  }
//    active.foldLeft[Decimal](amount) {
//      case (left, sidePot) ⇒
//        sidePot add (member, left)
//    }
  
  def add(member: Player, amount: Decimal, isAllIn: Boolean = false): Decimal = {
    val left = allocate (member, amount)
    
    if (isAllIn) {
      split (member, left)
      0
    } else current add (member, left)
  }
  
  override def toString = {
    val s = new StringBuilder
    
    s.append(current.toString)
    sidePots foreach { sidePot =>
      s.append(sidePot.toString + "\n")
    }
    
    s.toString()
  }
}
