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
    val value: Decimal = members getOrElse (member, .0)
    val bet = value + left
    members += (member -> bet)

    val _new = new SidePot(cap map { amt ⇒ amt - bet })
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
      members.map {
        case (member, amount) ⇒
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
  var main: SidePot = new SidePot
  var side: List[SidePot] = List.empty
  var inactive: List[SidePot] = List.empty

  def total: Decimal = sidePots.map(_.total).sum

  def sidePots: List[SidePot] = {
    var pots = new mutable.ListBuffer[SidePot]
    if (main.isActive)
      pots += main
    (side ++ inactive) foreach { side ⇒
      if (side.isActive)
        pots += side
    }
    pots.toList
  }

  import de.pokerno.util.ConsoleUtils._

  def split(member: Player, amount: Decimal) {
    if (amount < 0) {
      info("splitting side=%s for member %s with amount %s", side.head, member, amount)
      val current = side.head
      side = side drop 1
      val (_new, _old) = current split (member, amount, current.cap)
      side ++= List(_old, _new)
    } else {
      info("splitting main=%s for member %s with amount %s", main, member, amount)
      val (_new, _old) = main split (member, amount)
      side :+= _old
      main = _new
    }
    //error("main=%s\nside=%s", main, side)
  }

  def complete() {
    inactive ++= side ++ List(main)
    main = new SidePot
    side = List.empty
  }

  private def allocate(member: Player, amount: Decimal) =
    side.foldLeft[Decimal](amount) {
      case (left, sidePot) ⇒
        sidePot add (member, left)
    }

  def add(member: Player, amount: Decimal, isAllIn: Boolean = false): Decimal = {
    val left = allocate (member, amount)

    if (isAllIn) {
      split (member, left)
      0
    } else main add (member, left)
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
