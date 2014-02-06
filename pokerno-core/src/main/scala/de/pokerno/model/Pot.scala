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

  def split(member: Player, left: Decimal, cap: Option[Decimal] = None): Tuple2[SidePot, SidePot] = {
    // TODO process when left < 0
    val value: Decimal = members getOrElse (member, .0) // 12,000
    if (member.id == "Infante" && value != 12000) {
      warn("members=%s", members)
      warn("assert failed! 1")
    }
    val bet = value + left // 11,000
    if (member.id == "Infante" && bet != 11000) {
      warn("assert failed! 2")
    }
    members += (member -> bet)

    val _new = new SidePot(cap map { amt => amt - bet })
    _new.members = members.
      filter { case (key, _value) ⇒ _value > bet && key != member }. // [12,000]
      map { case (key, _value) ⇒ (key, _value - bet) } // [Fukutu -> 1,000]

    val _old = new SidePot(Some(bet))
    _old.members = members map {
      case (key, _value) ⇒
        (key, List(_value, bet).min) // [Bond -> 6,000; Fukutu -> 11,000, Infante -> 11,000, LeChiffre -> 11,000]
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
    if (!current.isActive) {
      warn("main is not active; using %s", active.head)
      current = active.head
      active = active.drop(1)
      
      val (_new, _old) = current split (member, amount, current.cap)
      
      active ++= List(_new, _old)
    } else {
      info("splitting current=%s for member %s with amount %s", current, member, amount)
      val (_new, _old) = current split (member, amount, None)
      
      active :+= _old
      current = _new
    }
  }
  
  def complete() {
    inactive ++= active ++ List(current)
    current = new SidePot
    active = List.empty
  }
  
  def allocate(member: Player, amount: Decimal) = {
    active.foldRight[Decimal](amount) {
      case (sidePot, left) ⇒
        val l = sidePot add (member, left)
        warn("left=%s leftAfter=%s for member %s in pot %s", left, l, member, sidePot)
        l
    }
  }
  
  def add(member: Player, amount: Decimal, isAllIn: Boolean = false): Decimal = {
    val left = allocate (member, amount)
    
    if (isAllIn) {
      split (member, left)
      0
    } else current add (member, left)
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
