package de.pokerno.model

import java.util.Locale
import com.fasterxml.jackson.annotation.JsonValue

class SidePot(val capFrom: Decimal = 0, val cap: Option[Decimal] = None) {
  // members
  var members = collection.mutable.Map[Player, Decimal]()
  
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
  
  def uncalled(): Option[Tuple2[Player, Decimal]] = {
    if (members.size == 1) {
      return members.headOption
    }
    if (members.size > 1) {
      val sorted = members.toSeq.sortBy(_._2).reverse
      val max = sorted.head // max bet
      val (called, under) = sorted.span(_._2 == max._2) // how much called max bet
      if (called.size == 1)
        return Some(max._1, max._2 - under.head._2)
    }
    None
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