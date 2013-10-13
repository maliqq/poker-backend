package pokerno.backend.model

import scala.math.{ BigDecimal ⇒ Decimal }
import scala.collection.mutable.ListBuffer

class SidePot(val cap: Option[Decimal] = None) {
  var members: Map[Player, Decimal] = Map.empty

  def total: Decimal = members.values sum

  def isActive: Boolean = members.size > 0 && total > .0

  def add(member: Player, amount: Decimal): Decimal = {
    if (amount == .0)
      return .0

    val value: Decimal = members getOrElse (member, .0)

    if (!cap.isDefined) {
      members += (member -> (value + amount))
      return .0
    }

    if (cap.get >= amount) {
      members += (member -> cap.get)
      return value + amount - cap.get
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
        s ++= "%s: %.2f" format (member, amount)
    }
    s + "-- Total: %.2f Cap: %.2f" format (total, cap)
  }
}

class Pot {
  var main: SidePot = new SidePot
  var side: List[SidePot] = List.empty

  def total: Decimal = sidePots map (_.total) sum

  def sidePots: List[SidePot] = {
    val pots: ListBuffer[SidePot] = new ListBuffer
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

  def add(member: Player, amount: Decimal, allIn: Boolean = false) = side.foldRight[Decimal](.0) {
    case (p, acc) ⇒ p add (member, acc)
  }
  
  def <<-(member: Player, amount: Decimal) {
    val left = add(member, amount)
    split(member, left)
  }
  
  def <<(member: Player, amount: Decimal) {
    val left = add(member, amount)
    main add (member, left)
  }

  override def toString = {
    val s = new StringBuilder
    sidePots foreach { sidePot ⇒ s ++= sidePot.toString }
    s toString
  }
}
