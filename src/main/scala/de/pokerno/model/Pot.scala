package de.pokerno.model

import scala.math.{ BigDecimal ⇒ Decimal }
import scala.collection.mutable

class SidePot(val cap: Option[Decimal] = None) {
  var members: Map[Player, Decimal] = Map.empty

  def total: Decimal = members.values sum

  def isActive: Boolean = members.size > 0 && total > .0

  def add(member: Player, amount: Decimal): Decimal = {
    if (amount == .0) return .0

    val value: Decimal = members.getOrElse(member, .0)
    val newValue = value + amount

    if (!cap.isDefined) {
      members += (member -> newValue)
      return .0
    }

    if (amount >= cap.get) {
      members += (member -> cap.get)
      return newValue - cap.get
    }

    amount
  }

  def split(member: Player, left: Decimal): Tuple2[SidePot, SidePot] = {
    val value: Decimal = members getOrElse (member, .0)
    val bet = value + left
    members += (member -> bet)

    val main = new SidePot
    main.members = members.
      filter { case (key, value) ⇒ value > bet && key != member }.
      map { case (key, value) ⇒ (key, value - bet) }

    val side = new SidePot(Some(bet))
    side.members = members map {
      case (key, value) ⇒
        (key, List(value, bet).min)
    }

    (main, side)
  }

  override def toString = {
    val s = new StringBuilder
    members foreach {
      case (member, amount) ⇒
        s ++= "%s: %.2f\n" format (member, amount)
    }
    s ++= "-- Total: %.2f" format (total)
    if (cap isDefined) s ++= " Cap: %.2f" format (cap get)
    s toString
  }
}

class Pot {
  var main: SidePot = new SidePot
  var side: List[SidePot] = List.empty

  def total: Decimal = sidePots map (_.total) sum

  def sidePots: List[SidePot] = {
    var pots = new mutable.ListBuffer[SidePot]
    if (main isActive)
      pots += main
    side foreach { side ⇒
      if (side isActive)
        pots += side
    }
    pots toList
  }

  def split(member: Player, amount: Decimal) {
    val (_main, _side) = main split (member, amount)
    side ++= List(_side)
    main = _main
  }

  def add(member: Player, amount: Decimal) = side.foldRight[Decimal](amount) {
    case (p, acc) ⇒ p add (member, acc)
  }

  override def toString = {
    val s = new StringBuilder
    s ++= main.toString
    sidePots foreach { sidePot ⇒ s ++= sidePot.toString }
    s toString
  }
}
